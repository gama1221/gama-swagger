package com.gama.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class SwaggerApplication {

    private static final Logger log = LoggerFactory.getLogger(SwaggerApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SwaggerApplication.class, args);
        logApplicationStartup(context.getEnvironment());
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        String serverPort = env.getProperty("server.port", "4545");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Unable to resolve local host address, defaulting to localhost");
        }

        String appName = env.getProperty("spring.application.name", "SwaggerApplication");
        String[] profiles = env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles();
        String apiDocsPath = env.getProperty("springdoc.api-docs.path", "/v3/api-docs");

        log.info("""
                
                ================================================================================
                  Application '{}' is running! Access URLs:
                  • Local Swagger UI: \t{}://localhost:{}{}/swagger-ui/index.html
                  • External UI:      \t{}://{}:{}{}/swagger-ui/index.html
                  • OpenAPI JSON:     \t{}://localhost:{}{}{}
                  • Active Profile(s):\t{}
                ================================================================================
                """,
                appName,
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath, apiDocsPath,
                String.join(", ", profiles)
        );
    }
}
