package de.securitysquad.webifier.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.UnknownHostException;

@Configuration
@EnableMongoRepositories(
        basePackages = "de.securitysquad.webifier.persistence.repository",
        includeFilters = @ComponentScan.Filter(
                value = {

                },
                type = FilterType.ASSIGNABLE_TYPE
        )
)
public class MongoConfig {
    @Bean
    public MongoTemplate mongoTemplate(Mongo mongo, @Value("${database.name}") String name) {
        return new MongoTemplate(mongo, name);
    }

    @Bean
    public Mongo mongo(@Value("${database.host}") String host, @Value("${database.port}") int port) throws UnknownHostException {
        return new MongoClient(host, port);
    }
}