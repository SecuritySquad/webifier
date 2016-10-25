package de.securitysquad.webifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by samuel on 25.10.16.
 */
@SpringBootApplication
@ComponentScan("de.securitysquad.webifier")
public class WebifierApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebifierApplication.class, args);
        context.registerShutdownHook();
    }
}