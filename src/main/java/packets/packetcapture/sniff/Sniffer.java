package packets.packetcapture.sniff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import packets.packetcapture.sniff.ardikars.NativeBridge;
import packets.packetcapture.sniff.assembly.Ip4Defragmenter;
import packets.packetcapture.sniff.assembly.TcpStreamBuilder;
import packets.packetcapture.sniff.assembly.TcpStreamErrorHandler;
import packets.packetcapture.sniff.netpackets.EthernetPacket;
import packets.packetcapture.sniff.netpackets.Ip4Packet;
import packets.packetcapture.sniff.netpackets.RawPacket;
import packets.packetcapture.sniff.netpackets.TcpPacket;
import pcap.spi.Interface;
import pcap.spi.Pcap;
import pcap.spi.Service;
import pcap.spi.Service.Creator;
import pcap.spi.option.DefaultLiveOptions;

public class Sniffer {

    private static final int REALM_PORT = 2050;
    private static final int ALT_REALM_PORT = 443;
    private static final int POLL_TIMEOUT_MS = 250;
    private static final int RECENT_TCP_PACKET_LIMIT = 4096;

    private final LinkedBlockingQueue<RawPacket> packetQueue;
    private final TcpStreamBuilder incoming;
    private final TcpStreamBuilder outgoing;
    private final List<Pcap> activeSniffers;
    private final Set<String> recentTcpPackets;
    private volatile boolean stop;
    private volatile boolean exitLagActive;

    public Sniffer(PProcessor packetProcessor) {
        packetQueue = new LinkedBlockingQueue<>();
        activeSniffers = Collections.synchronizedList(new ArrayList<>());
        recentTcpPackets = Collections.newSetFromMap(
            new LinkedHashMap<String, Boolean>(
                RECENT_TCP_PACKET_LIMIT,
                0.75f,
                true
            ) {
                @Override
                protected boolean removeEldestEntry(
                    Map.Entry<String, Boolean> eldest
                ) {
                    return size() > RECENT_TCP_PACKET_LIMIT;
                }
            }
        );
        incoming = new TcpStreamBuilder(
            packetProcessor::resetIncoming,
            packetProcessor::incomingStream
        );
        outgoing = new TcpStreamBuilder(
            packetProcessor::resetOutgoing,
            packetProcessor::outgoingStream
        );
    }

    public void startSniffer() throws Exception {
        Service service = Creator.create("PcapService");
        Interface[] interfaces = NativeBridge.getInterfaces(service);
        exitLagActive = ExitLagDetector.isExitLagActive(interfaces);
        Arrays.sort(interfaces, interfacePriorityComparator(exitLagActive));

        stop = false;
        packetQueue.clear();
        recentTcpPackets.clear();
        activeSniffers.clear();

        for (Interface networkInterface : interfaces) {
            if (shouldSkip(networkInterface, exitLagActive)) {
                continue;
            }

            Thread thread = new Thread(
                () -> startPcap(service, networkInterface, exitLagActive),
                "Tomato packet capture - " + interfaceName(networkInterface)
            );
            thread.setDaemon(true);
            thread.start();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        processBufferedPackets();
    }

    private void startPcap(
        Service service,
        Interface networkInterface,
        boolean exitLagActive
    ) {
        try {
            DefaultLiveOptions options = new DefaultLiveOptions();
            options.timeout(1);
            options.immediate(true);

            Pcap pcap = service.live(networkInterface, options);
            if (pcap == null) {
                return;
            }

            pcap.setFilter(captureFilter(exitLagActive), true);
            activeSniffers.add(pcap);
            startPacketSniffer(pcap);
        } catch (Exception ignored) {
            // Some virtual adapters fail to open. Other interfaces may still be valid.
        }
    }

    private void startPacketSniffer(Pcap pcap) {
        NativeBridge.PacketListener packetListener = rawPacket -> receivedRawPacket(rawPacket);
        try {
            NativeBridge.loop(pcap, -1, packetListener);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null || !message.contains("Break loop")) {
                e.printStackTrace();
            }
        }
    }

    public void closeUnusedSniffers() {
        // Capture may legitimately move between interfaces while tunnels are active.
    }

    private void processBufferedPackets() {
        while (!stop) {
            try {
                RawPacket rawPacket = packetQueue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (rawPacket != null) {
                    processRawPacket(rawPacket);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void receivedRawPacket(RawPacket rawPacket) {
        TcpStreamErrorHandler.INSTANCE.logTCPPacket(rawPacket);
        if (rawPacket != null && computeChecksum(rawPacket.getPayload())) {
            packetQueue.offer(rawPacket);
        }
    }

    private void processRawPacket(RawPacket rawPacket) {
        try {
            EthernetPacket ethernetPacket = rawPacket.getNewEthernetPacket();
            if (ethernetPacket == null) {
                return;
            }

            Ip4Packet ip4Packet = ethernetPacket.getNewIp4Packet();
            Ip4Packet defragmentedPacket = Ip4Defragmenter.defragment(ip4Packet);
            if (defragmentedPacket == null) {
                return;
            }

            TcpPacket tcpPacket = defragmentedPacket.getNewTcpPacket();
            if (tcpPacket != null) {
                receivedPackets(tcpPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receivedPackets(TcpPacket tcpPacket) {
        if (!recentTcpPackets.add(packetKey(tcpPacket))) {
            return;
        }

        if (tcpPacket.getSrcPort() == REALM_PORT) {
            incoming.streamBuilder(tcpPacket);
        } else if (tcpPacket.getDstPort() == REALM_PORT) {
            outgoing.streamBuilder(tcpPacket);
        } else if (exitLagActive && tcpPacket.getSrcPort() == ALT_REALM_PORT) {
            incoming.streamBuilder(tcpPacket);
        } else if (exitLagActive && tcpPacket.getDstPort() == ALT_REALM_PORT) {
            outgoing.streamBuilder(tcpPacket);
        }
    }

    private static String captureFilter(boolean exitLagActive) {
        if (exitLagActive) {
            return "tcp port " + REALM_PORT + " or tcp port " + ALT_REALM_PORT;
        }
        return "tcp port " + REALM_PORT;
    }

    private static boolean computeChecksum(byte[] payload) {
        return true;
    }

    private static String packetKey(TcpPacket tcpPacket) {
        Ip4Packet ip4Packet = tcpPacket.getIp4Packet();
        return Arrays.hashCode(ip4Packet.getSrcAddr()) +
            ":" +
            Arrays.hashCode(ip4Packet.getDstAddr()) +
            ":" +
            tcpPacket.getSrcPort() +
            ":" +
            tcpPacket.getDstPort() +
            ":" +
            tcpPacket.getSequenceNumber() +
            ":" +
            tcpPacket.getAcknowledgmentNumber() +
            ":" +
            tcpPacket.getPayloadSize() +
            ":" +
            Arrays.hashCode(tcpPacket.getPayload());
    }

    public void closeSniffers() {
        stop = true;
        synchronized (activeSniffers) {
            for (Pcap pcap : activeSniffers) {
                try {
                    pcap.breakLoop();
                    pcap.close();
                } catch (Exception ignored) {
                }
            }
            activeSniffers.clear();
        }
    }

    private static Comparator<Interface> interfacePriorityComparator(
        boolean exitLagActive
    ) {
        return (first, second) -> {
            int firstScore = interfacePriorityScore(first, exitLagActive);
            int secondScore = interfacePriorityScore(second, exitLagActive);
            return Integer.compare(firstScore, secondScore);
        };
    }

    private static int interfacePriorityScore(
        Interface networkInterface,
        boolean exitLagActive
    ) {
        String description = interfaceDescription(networkInterface);
        String name = interfaceName(networkInterface);
        if (
            description.contains("loopback") ||
            name.contains("loopback") ||
            name.contains("npcap")
        ) {
            return 0;
        }
        if (
            exitLagActive &&
            (description.contains("exitlag") ||
                name.contains("exitlag") ||
                description.contains("wintun") ||
                name.contains("wintun") ||
                description.contains("tunnel") ||
                name.contains("tunnel") ||
                description.contains("tap") ||
                name.contains("tap") ||
                description.contains("vpn") ||
                name.contains("vpn"))
        ) {
            return 1;
        }
        return 2;
    }

    private static boolean shouldSkip(
        Interface networkInterface,
        boolean exitLagActive
    ) {
        String description = interfaceDescription(networkInterface);
        String name = interfaceName(networkInterface);
        if (description.contains("wan miniport") || name.contains("wan miniport")) {
            if (
                exitLagActive &&
                (description.contains("exitlag") || name.contains("exitlag"))
            ) {
                return false;
            }
            return true;
        }
        return false;
    }

    private static String interfaceDescription(Interface networkInterface) {
        String description = networkInterface.description();
        return description == null ? "" : description.toLowerCase();
    }

    private static String interfaceName(Interface networkInterface) {
        String name = networkInterface.name();
        return name == null ? "" : name.toLowerCase();
    }
}
