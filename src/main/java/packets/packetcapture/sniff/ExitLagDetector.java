package packets.packetcapture.sniff;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import pcap.spi.Interface;

final class ExitLagDetector {

    private static final int COMMAND_TIMEOUT_MS = 1500;

    private static final List<String> EXITLAG_PROCESS_NAMES = Arrays.asList(
        "exitlag.exe",
        "exitlagservice.exe",
        "exitlaglauncher.exe",
        "exitlagupdater.exe"
    );

    private ExitLagDetector() {}

    static boolean isExitLagActive(Interface[] pcapInterfaces) {
        try {
            return isExitLagActive(
                System.getProperty("os.name"),
                pcapInterfaces,
                new DefaultCommandRunner()
            );
        } catch (Exception ignored) {
            return false;
        }
    }

    static boolean isExitLagActive(
        String osName,
        Interface[] pcapInterfaces,
        CommandRunner commandRunner
    ) {
        if (!isWindows(osName)) {
            return false;
        }

        boolean processRunning = isAnyProcessRunning(
            EXITLAG_PROCESS_NAMES,
            commandRunner
        );
        if (!processRunning) {
            return false;
        }

        boolean hasLikelyAdapter = hasLikelyExitLagAdapter(pcapInterfaces);
        if (hasLikelyAdapter) {
            return true;
        }

        String ipconfigAll = commandRunner.run(
            Arrays.asList("cmd", "/c", "ipconfig", "/all"),
            COMMAND_TIMEOUT_MS
        );
        return containsIgnoreCase(ipconfigAll, "exitlag");
    }

    static boolean hasLikelyExitLagAdapter(Interface[] pcapInterfaces) {
        if (pcapInterfaces == null) {
            return false;
        }
        for (Interface networkInterface : pcapInterfaces) {
            if (networkInterface == null) {
                continue;
            }
            String name = safeLower(networkInterface.name());
            String description = safeLower(networkInterface.description());
            if (name.contains("exitlag") || description.contains("exitlag")) {
                return true;
            }
            if (name.contains("wintun") || description.contains("wintun")) {
                return true;
            }
            if (name.contains("tap") || description.contains("tap")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnyProcessRunning(
        List<String> processNames,
        CommandRunner commandRunner
    ) {
        String taskList = commandRunner.run(
            Arrays.asList("cmd", "/c", "tasklist", "/FO", "CSV", "/NH"),
            COMMAND_TIMEOUT_MS
        );
        String lower = safeLower(taskList);
        for (String processName : processNames) {
            if (lower.contains(processName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWindows(String osName) {
        if (osName == null) {
            return false;
        }
        return osName.toLowerCase(Locale.ROOT).contains("windows");
    }

    interface CommandRunner {
        String run(List<String> command, int timeoutMs);
    }

    private static final class DefaultCommandRunner implements CommandRunner {
        @Override
        public String run(List<String> command, int timeoutMs) {
            try {
                Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

                boolean finished = process.waitFor(
                    timeoutMs,
                    TimeUnit.MILLISECONDS
                );
                if (!finished) {
                    process.destroyForcibly();
                    return "";
                }

                Charset charset = Charset.defaultCharset();
                StringBuilder sb = new StringBuilder();
                try (
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), charset)
                    )
                ) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                }
                return sb.toString();
            } catch (Exception ignored) {
                return "";
            }
        }
    }

    private static String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private static boolean containsIgnoreCase(String text, String needle) {
        if (text == null || needle == null) {
            return false;
        }
        return safeLower(text).contains(safeLower(needle));
    }
}

