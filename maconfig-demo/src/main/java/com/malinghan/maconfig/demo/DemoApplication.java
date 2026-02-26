package com.malinghan.maconfig.demo;

import com.malinghan.maconfig.client.annotation.EnableMaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DemoProperties.class)
@EnableMaConfig(app = "demo-app", env = "dev", ns = "default", serverUrl = "http://127.0.0.1:9091")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
