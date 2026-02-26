package com.malinghan.maconfig.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DemoController {

    // 演示 @Value 热更新
    @Value("${demo.message:Hello World}")
    private String message;

    @Value("${demo.version:1.0}")
    private String version;

    // 演示 @ConfigurationProperties 热更新
    private final DemoProperties demoProperties;

    @GetMapping("/demo")
    public Map<String, String> demo() {
        return Map.of(
                "value.message", message,
                "value.version", version,
                "props.message", demoProperties.getMessage(),
                "props.version", demoProperties.getVersion()
        );
    }
}
