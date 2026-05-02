package packets.packetcapture.sniff;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ExitLagDetectorTest {

    @Test
    public void returnsFalseWhenNotWindows() {
        ExitLagDetector.CommandRunner runner = new FakeRunner("", "");
        boolean active = ExitLagDetector.isExitLagActive("Linux", null, runner);
        Assert.assertFalse(active);
    }

    @Test
    public void returnsFalseWhenExitLagProcessNotRunning() {
        ExitLagDetector.CommandRunner runner = new FakeRunner(
            "\"chrome.exe\",\"1234\",\"Console\",\"1\",\"120,000 K\"",
            "Windows IP Configuration\n"
        );
        boolean active = ExitLagDetector.isExitLagActive(
            "Windows 11",
            null,
            runner
        );
        Assert.assertFalse(active);
    }

    @Test
    public void returnsTrueWhenExitLagProcessRunningAndIpconfigMentionsExitLag() {
        ExitLagDetector.CommandRunner runner = new FakeRunner(
            "\"ExitLag.exe\",\"1234\",\"Console\",\"1\",\"120,000 K\"",
            "Windows IP Configuration\nEthernet adapter ExitLag:\n"
        );
        boolean active = ExitLagDetector.isExitLagActive(
            "Windows 11",
            null,
            runner
        );
        Assert.assertTrue(active);
    }

    @Test
    public void returnsFalseForOtherGameProxies() {
        ExitLagDetector.CommandRunner runner = new FakeRunner(
            "\"wtfast.exe\",\"1234\",\"Console\",\"1\",\"120,000 K\"",
            "Windows IP Configuration\n"
        );
        boolean active = ExitLagDetector.isExitLagActive(
            "Windows 11",
            null,
            runner
        );
        Assert.assertFalse(active);
    }

    private static final class FakeRunner implements ExitLagDetector.CommandRunner {

        private final String tasklistOutput;
        private final String ipconfigOutput;

        private FakeRunner(String tasklistOutput, String ipconfigOutput) {
            this.tasklistOutput = tasklistOutput;
            this.ipconfigOutput = ipconfigOutput;
        }

        @Override
        public String run(List<String> command, int timeoutMs) {
            String joined = String.join(" ", command).toLowerCase();
            if (joined.contains("tasklist")) {
                return tasklistOutput;
            }
            if (joined.contains("ipconfig")) {
                return ipconfigOutput;
            }
            return "";
        }
    }
}

