package com.malinghan.maconfig.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "maconfig.fail-fast=false"
)
class DemoApplicationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void demo_endpoint_returns_local_defaults_when_server_unavailable() {
        @SuppressWarnings("unchecked")
        Map<String, String> body = restTemplate.getForObject(
                "http://localhost:" + port + "/demo", Map.class);
        assertThat(body).isNotNull();
        // local defaults from application.yml
        assertThat(body.get("message")).isEqualTo("Hello World (local default)");
        assertThat(body.get("version")).isEqualTo("1.0-local");
    }
}
