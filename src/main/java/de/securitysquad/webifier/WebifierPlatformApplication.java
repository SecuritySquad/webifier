package de.securitysquad.webifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by samuel on 25.10.16.
 */
@SpringBootApplication
@ComponentScan("de.securitysquad.webifier")
@PropertySource("classpath:application.properties")
public class WebifierPlatformApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebifierPlatformApplication.class, args);
        context.registerShutdownHook();
    }
}