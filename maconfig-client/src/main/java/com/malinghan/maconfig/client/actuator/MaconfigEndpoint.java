package com.malinghan.maconfig.client.actuator;

import com.malinghan.maconfig.client.MaConfigServiceImpl;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

@Endpoint(id = "maconfig")
public class MaconfigEndpoint {

    private final MaConfigServiceImpl service;

    public MaconfigEndpoint(MaConfigServiceImpl service) {
        this.service = service;
    }

    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("configCount", service.getConfigMap().size());
        result.put("configs", service.getConfigMap());
        return result;
    }
}
