package com.malinghan.maconfig.client.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malinghan.maconfig.client.ConfigMeta;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpMaRepository implements MaRepository {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, String> getConfigs(ConfigMeta meta) {
        try {
            String url = meta.getServerUrl() + "/v1/configs?app=" + meta.getApp()
                    + "&env=" + meta.getEnv() + "&ns=" + meta.getNs();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {});
            Object data = result.get("data");
            if (data == null) {
                return Map.of();
            }
            //noinspection unchecked
            return (Map<String, String>) data;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch configs from server", e);
        }
    }

    @Override
    public long getVersion(ConfigMeta meta) {
        try {
            String url = meta.getServerUrl() + "/v1/version?app=" + meta.getApp()
                    + "&env=" + meta.getEnv() + "&ns=" + meta.getNs();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {});
            Object data = result.get("data");
            if (data == null) {
                return 0L;
            }
            return ((Number) data).longValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch version from server", e);
        }
    }
}
