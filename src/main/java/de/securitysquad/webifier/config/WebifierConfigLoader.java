package de.securitysquad.webifier.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by samuel on 08.11.16.
 */
@Configuration
public class WebifierConfigLoader {
    private ObjectMapper mapper = new ObjectMapper();

    @Bean
    public WebifierConfig load() throws IOException {
        try {
            return loadExternal(WebifierConstants.Configuration.EXTERNAL);
        } catch (IOException e) {
            System.out.println("Loading internal configuration!");
            return loadInternal(WebifierConstants.Configuration.INTERNAL);
        }
    }

    private WebifierConfig loadExternal(String name) throws IOException {
        InputStream is = new FileInputStream(getExternalFile(name));
        return load(is);
    }

    private File getExternalFile(String name) {
        return new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent(), name);
    }

    private WebifierConfig loadInternal(String name) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(name);
        return load(is);
    }

    private WebifierConfig load(InputStream is) throws IOException {
        return mapper.readValue(is, WebifierConfig.class);
    }
}
