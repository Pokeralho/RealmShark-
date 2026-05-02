# RealmShark  
### A library/GUI packet sniffer for Realm of the Mad God built with Java.

RealmShark is a Java library/program created to read network packets at the kernel level without the the ability to modify, block or send packets. The library is EULA/copyright compliant because it does not use any game code or assets.  

Given RealmShark reads packets directly from the network adapter it can even be used to listen on a PC that is not running the game. It is an independent program from the game and is completely extendable/customizable.  

Multiple instances of Realm of the Mad God are not supported using this sniffer.  
As of now the sniffer cannot filter packets from multiple instances of the game running at the same time.   
The sniffer crashes if it cannot distinguish the packets from different instances at the network layer.  
In the future, OS specific functionality will be added to support multiple instances of clients.

## Tomato v1.9.4 notes

This fork includes Tomato-focused fixes for real-time capture/UI refresh, local-player DPS accounting, loot drop snapshotting, and configurable dungeon drop/modifier alerts. See [CHANGELOG.md](CHANGELOG.md) for the full v1.9.4 change list and validation notes.

### ExitLag / proxies (Windows)

Quando o ExitLag está ativo, alguns setups acabam “movendo” o tráfego do jogo para uma interface/encapsulamento diferente do padrão, o que pode fazer o sniffer deixar de ver os pacotes.

Este fork inclui uma detecção automática (best-effort) do ExitLag e ajustes no filtro/prioridade de interfaces para manter a captura funcionando:

- Detecta ExitLag via processos (`ExitLag.exe`, `ExitLagService.exe`, etc.) e evidências de adaptador (interfaces do pcap / `ipconfig /all`).
- Se identificado, amplia o filtro de captura para incluir `tcp/2050` e `tcp/443` e prioriza interfaces típicas de túnel/VPN (incluindo `ExitLag`, `Wintun`, `TAP`).
- Se o ExitLag não for identificado (ou se a detecção falhar), o sniffer continua com o comportamento padrão (apenas `tcp/2050`).

#### Credits:

- Most backed code was written by [Cortex](https://github.com/MCRcortex). Huge thanks to him.
- Inspired by work done by [abrn](https://github.com/abrn/realmlib) and [thomas-crane](https://github.com/thomas-crane/realmlib-net).
- [ardikars](https://github.com/ardikars/pcap) library for packet processing.
- [libpcap or winpcap](https://npcap.com/) is used by the ardikars library to make the packet sniffing possible.

## Install guide

If there are errors running the program described below, please check the [Discord](https://discord.gg/msKrWSD4Mf), the troubleshooting section below, or [open an issue here,](https://github.com/X-com/RealmShark/issues) so it can be resolved.

### For Windows:

1. Java and Npcap is required for running the program. Java can be downloaded from [here](https://www.java.com/en/download/) and Npcap from [here](https://npcap.com/#download). Open the files one at a time and follow the install instructions for both.

2. Download the latest `Tomato-v*.jar` file from [Releases](https://github.com/X-com/RealmShark/releases). Only need the *.jar file.

- Java download [image](https://user-images.githubusercontent.com/5974568/183230180-f9a66d31-2ed4-4073-8af2-cda12f271d01.png).
- Npcap download [image](https://user-images.githubusercontent.com/5974568/183230181-b8eacef2-71f3-47f5-8d46-959eb1bb82bf.png).
- Jar download [image](https://user-images.githubusercontent.com/5974568/183230231-b47f588a-08be-42f1-942f-8f0facf41aa0.png).  

3. Run the program by simply opening the downloaded Tomato-v*.jar file.  

4. The RealmShark GUI should open. Start it by clicking File -> Start Sniffer. All chat in the game should appear in the Chat tab.

### For Mac:
All the steps are the same, but in order to enable packet capture for the non-root user, you may need to run:
`sudo chown $(whoami):admin /dev/bpf*`

### For Linux:
Linux is not officially supported, but many users have had success. Steam is the standardized client for Linux; other installations may not work as expected. If you run into issues, try:

- Run from command line using the `--path <custom-path>` argument.
- Ask for help in the Discord.

## Troubleshooting guide

Windows troubleshooting guide:

Some Windows 11 users have issues with Npcap 1.70. Try and uninstall 1.70 and install the 1.60 version. Link found here, [Npcap 1.60](https://www.mediafire.com/file/xkjmfz1v1b47e0a/npcap-1.60.exe/file).

If the Tomato-v*.jar file does nothing double clicking it after following the installation guide above. Open powershell or CMD in the folder where the Tomato-v*.jar file is located.

1. Open File Explorer and navigate to the folder where the *.jar is located.
2. Right click in the empty space inside the folder, while holding shift. Then select Powershell or CMD. [Example image](https://user-images.githubusercontent.com/5974568/183230822-a35e2c52-8235-4efa-8543-9219b4611adc.png)
3. If PowerShell is opened. type "cmd" and press Enter. If CMD is opened, skip to step 4.
4. Type "java -jar ". Make sure to add space after "-jar ".
5. Press tab several times until the Tomato-v*.jar name appears. Then press Enter. [Example image](https://user-images.githubusercontent.com/5974568/183231024-a1e006b7-7dd0-43f3-8a99-4fdee3827f94.png)

If the program starts without problems it means you have issues with your register keys. To fix your register to not need command prompt to start the program a simple jarfix is needed. If the program still doesn't start, report the bug in the [issues here](https://github.com/X-com/RealmShark/issues). Try to include as much information as possible in the report.

1. Download the jarfix from [here](https://johann.loefflmann.net/en/software/jarfix/index.html). Image of file [here](https://user-images.githubusercontent.com/5974568/183231327-ac0a33c7-edb4-41bb-897f-bb86fa9ab939.png).
2. Run it as Administrator. Example of running the jarfix [here](https://user-images.githubusercontent.com/5974568/183231330-9d53b0b9-8288-4cab-a726-4095f3e3f479.png).
3. Start the program by double clicking Tomato-v*.jar.

If you can start the program, but you can not see any chat messages from ingame chat after starting the sniffer.

1. Open the program in console prompt described above.
2. Start the sniffer and check console for error messages.
3. If you get an error stating "The pcap_t has not been activated" similar to this [image](https://user-images.githubusercontent.com/5974568/183231488-c79f0189-4513-4b06-85d7-17deb610a340.png) your network interface is faulty or you are missing a Loopback Adapter. 
4. Follow this guide, link [here](https://tencomputer.com/npcap-loopback-adapter-no-internet/), for repairing your network interface (recommending to do the steps in inverted order starting with step 5). Do one step at a time and check if it fixes the problem before trying the next.
5. If it still doesn't fix the problem follow a youtube guide to install a Loopback Adapter [here](https://www.youtube.com/watch?v=N3Ido5VEkNE).

If any other problem shows up. Please report them in the issues tracker found [here](https://github.com/X-com/RealmShark/issues) to have it resolved. Make sure to include any console outputs, version of java installed (type "java -v" in console to get the version), windows version and other reproduction steps.

## Building from source!

This is written for use with IntelliJ IDEA only. Other IDEs can also  be used in a similar manner.

You will need an OpenJDK 8 SDK install from [here.](https://jdk.java.net/18/) If you are using IntelliJ IDEA, IntelliJ can install the JDK automatically.

IntelliJ IDEA can be found [here](https://www.jetbrains.com/idea/download/#section=windows). Download the free community edition.

Download the .zip or clone the repo via CLI:
`git clone https://github.com/X-com/RealmShark && cd RealmShark`

Open IntelliJ and click **File > New > Project from Existing Sources...**
Navigate to the realm shark source folder and open build.gradle. Alternatively drag and drop the build.gradle into the IntelliJ window and then double click on it. IntelliJ will automatically install gradle and setup the project.

The application can now be built, ran and debugged from IntelliJ.

To build a runnable jar. Use the gradle tool named shadowJar. The [shadowJar](https://user-images.githubusercontent.com/5974568/185830689-3031bb23-7d6c-416f-984d-4d460f15140c.png) will build a runnable jar and place it in the build/libs folder. The shadowJar build tool builds a fat jar including all the resources. Note! This includes any files that can't be red from within the jar, i.e. dll files.
 - If resources have to be extracted out of the jar during runtime. An example class called LibExtractor.java is found in the project to help extract resources out of the jar during runtime in case it is needed.
 - Both application name and the version can be modified in the build.gradle folder. Simply modify the applicationName to change the release name or project.version to change the release version.

### Dev build (CLI) nesta branch

Esta branch (`tomato-v1.9.3-realtime-loot-alerts`) depende do jar da biblioteca RealmShark (`RealmShark-v1.2.2.jar`) no diretório `libs/` (o diretório `libs/` é ignorado pelo git).

Passo-a-passo:

1. Build do RealmShark (biblioteca)
   - `git checkout realmshark`
   - `gradle shadowJar`
   - Copie `build/libs/RealmShark-v1.2.2.jar` para `libs/RealmShark-v1.2.2.jar` na branch do Tomato.

2. Build do Tomato (este projeto)
   - `git checkout tomato-v1.9.3-realtime-loot-alerts`
   - `gradle test shadowJar`
   - O jar final fica em `build/libs/Tomato-v1.9.3.jar`

### Validação (3 cenários)

- ExitLag inativo
  - Abrir o jogo, iniciar o Tomato, clicar em `File -> Start Sniffer` e validar que o chat aparece na aba Chat.
- ExitLag ativo
  - Abrir o ExitLag, ativar o perfil do RotMG, abrir o jogo, iniciar o Tomato e validar novamente o chat/packets.
  - Se não aparecer, abrir o Tomato via console (`java -jar Tomato-v*.jar`) e verificar erros do pcap/Npcap conforme a seção de troubleshooting.
- Outros proxies/VPNs de jogos (compatibilidade)
  - Repetir o fluxo acima com o proxy/VPN ativo e validar que a captura não degrada quando o ExitLag não está presente (comportamento padrão permanece `tcp/2050`).
