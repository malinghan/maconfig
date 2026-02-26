package com.malinghan.maconfig.demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {
    private String message = "Hello World (local default)";
    private String version = "1.0-local";
}
