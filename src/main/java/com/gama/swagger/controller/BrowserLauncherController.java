package com.gama.swagger.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/browser")
@Tag(name = "Browser Launcher", description = "Endpoints showing BrowserLauncher workflow, execution steps, and environment configuration")
public class BrowserLauncherController {

    @Value("${openProject.isOpen:true}")
    private boolean isOpen;

    @Value("${openProject.web.openUrl:http://localhost:4545/swagger-ui/index.html}")
    private String openUrl;

    public record LauncherStep(
            int step,
            String name,
            String condition,
            String action,
            String details
    ) {}

    public record BrowserStepsResponse(
            String component,
            String description,
            Map<String, Object> configuration,
            Map<String, Object> currentEnvironment,
            List<LauncherStep> steps
    ) {}

    @GetMapping("/steps")
    @Operation(
            summary = "Get BrowserLauncher steps",
            description = "Returns the sequential workflow steps implemented in BrowserLauncher.java along with the current runtime environment state."
    )
    public ResponseEntity<BrowserStepsResponse> getBrowserLauncherSteps() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        boolean xdgAvailable = isCommandAvailable("xdg-open");
        boolean desktopSupported = !GraphicsEnvironment.isHeadless() && Desktop.isDesktopSupported();

        String selectedBranch;
        if (!isOpen) {
            selectedBranch = "Auto-open disabled (openProject.isOpen=false)";
        } else if (os.contains("win")) {
            selectedBranch = "Windows: cmd /c start \"\" " + openUrl;
        } else if (os.contains("mac")) {
            selectedBranch = "macOS: open " + openUrl;
        } else if (os.contains("nux") || os.contains("nix") || os.contains("aix")) {
            if (xdgAvailable) {
                selectedBranch = "Linux/Unix: xdg-open " + openUrl;
            } else {
                selectedBranch = "Linux/Unix (fallback): Desktop.browse (" + openUrl + ")";
            }
        } else {
            selectedBranch = "Unrecognized OS (fallback): Desktop.browse (" + openUrl + ")";
        }

        Map<String, Object> configuration = new LinkedHashMap<>();
        configuration.put("openProject.isOpen", isOpen);
        configuration.put("openProject.web.openUrl", openUrl);

        Map<String, Object> environment = new LinkedHashMap<>();
        environment.put("detectedOs", os);
        environment.put("isHeadless", GraphicsEnvironment.isHeadless());
        environment.put("isDesktopSupported", desktopSupported);
        environment.put("isXdgOpenAvailable", xdgAvailable);
        environment.put("selectedExecutionBranch", selectedBranch);

        List<LauncherStep> steps = List.of(
                new LauncherStep(
                        1,
                        "Check Enablement Flag",
                        "if (!isOpen)",
                        "Verify whether browser auto-launch is enabled via property 'openProject.isOpen'.",
                        "If isOpen is false, logs 'Auto-open browser disabled' and exits run() method immediately."
                ),
                new LauncherStep(
                        2,
                        "Operating System Detection",
                        "System.getProperty(\"os.name\", \"\").toLowerCase(Locale.ROOT)",
                        "Inspect system OS properties to determine the host environment platform.",
                        "Identifies whether current system matches Windows ('win'), macOS ('mac'), or Linux/Unix ('nux', 'nix', 'aix')."
                ),
                new LauncherStep(
                        3,
                        "OS-Specific Browser Launch Command",
                        "Platform branch matching detected OS",
                        "Dispatches the appropriate CLI command via Runtime.getRuntime().exec().",
                        "Windows: ['cmd', '/c', 'start', '', openUrl] | macOS: ['open', openUrl] | Linux/Unix: verifies 'which xdg-open', then executes ['xdg-open', openUrl] if found."
                ),
                new LauncherStep(
                        4,
                        "Desktop API Fallback (fallbackDesktopBrowse)",
                        "xdg-open missing on Linux, or unrecognized OS",
                        "Attempts to use Java AWT Desktop.getDesktop().browse(new URI(openUrl)).",
                        "Checks Desktop.isDesktopSupported() and Desktop.Action.BROWSE. If headless or unsupported, logs manual URL instruction."
                ),
                new LauncherStep(
                        5,
                        "Error Handling and Logging",
                        "try-catch (Exception e)",
                        "Catches process execution or browser launch exceptions to protect application lifecycle.",
                        "Logs warnings with error message without failing the Spring Boot application startup."
                )
        );

        BrowserStepsResponse response = new BrowserStepsResponse(
                "BrowserLauncher (CommandLineRunner)",
                "Automatically opens the configured Swagger UI URL in the default system browser upon application startup.",
                configuration,
                environment,
                steps
        );

        return ResponseEntity.ok(response);
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

