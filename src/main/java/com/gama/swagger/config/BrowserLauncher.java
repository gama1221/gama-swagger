package com.gama.swagger.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;
import java.util.Locale;

@Component
public class BrowserLauncher implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    @Value("${openProject.isOpen:true}")
    private boolean isOpen;

    @Value("${openProject.web.openUrl}")
    private String openUrl;

    @Override
    public void run(String... args) {
        if (!isOpen) {
            log.info("Browser auto-open is disabled via configuration (openProject.isOpen=false).");
            return;
        }

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "");
        log.info("System environment detected: OS='{}', Arch='{}'", os, arch);

        try {
            if (os.contains("win")) {
                log.info("Dispatching browser open command for Windows: [cmd /c start \"\" {}]", openUrl);
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", openUrl});
            } else if (os.contains("mac")) {
                log.info("Dispatching browser open command for macOS: [open {}]", openUrl);
                Runtime.getRuntime().exec(new String[]{"open", openUrl});
            } else if (os.contains("nux") || os.contains("nix") || os.contains("aix")) {
                // Linux / Unix
                if (isCommandAvailable("xdg-open")) {
                    log.info("Dispatching browser open command for Linux/Unix: [xdg-open {}]", openUrl);
                    Runtime.getRuntime().exec(new String[]{"xdg-open", openUrl});
                } else {
                    log.warn("Command 'xdg-open' not found in system PATH. Attempting Java Desktop API fallback...");
                    fallbackDesktopBrowse();
                }
            } else {
                log.warn("Unrecognized OS '{}' — attempting Java Desktop API fallback...", os);
                fallbackDesktopBrowse();
            }
            log.info("Browser auto-launch request submitted successfully for target URL: {}", openUrl);
        } catch (Exception e) {
            log.warn("Could not auto-open browser: {}", e.getMessage());
        }
    }

    private void fallbackDesktopBrowse() {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                log.info("Opening target URL via Java Desktop API: {}", openUrl);
                Desktop.getDesktop().browse(new URI(openUrl));
                log.info("Browser successfully opened via Desktop.browse fallback.");
            } else {
                log.warn("Headless environment or Desktop browsing not supported. Please open manually: {}", openUrl);
            }
        } catch (Exception ex) {
            log.warn("Desktop.browse fallback failed: {}", ex.getMessage());
        }
    }

    private boolean isCommandAvailable(String cmd) {
        try {
            Process p = new ProcessBuilder("which", cmd)
                    .redirectErrorStream(true)
                    .start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
