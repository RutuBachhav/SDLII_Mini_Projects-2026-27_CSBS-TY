package com.lostlink;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RestartTest {
  @TempDir Path dir;

  @Test
  void separateJvmProcessesPersistData() throws Exception {
    runModes("write", "read");
  }

  @Test
  void claimsRecoveryNotesPhotosAndDemoMarkerSurviveProcessRestart() throws Exception {
    runModes("workflow-write", "workflow-read");
  }

  private void runModes(String... modes) throws Exception {
    for (String mode : modes) {
      Process p =
          new ProcessBuilder(
                  Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                  "--enable-native-access=ALL-UNNAMED",
                  "-cp",
                  System.getProperty("java.class.path"),
                  "com.lostlink.RestartProbe",
                  dir.toString(),
                  mode)
              .redirectErrorStream(true)
              .start();
      boolean exited = p.waitFor(20, TimeUnit.SECONDS);
      if (!exited) p.destroyForcibly();
      assertTrue(exited, "Restart probe timed out");
      String output = new String(p.getInputStream().readAllBytes());
      assertEquals(0, p.exitValue(), output);
    }
  }
}
