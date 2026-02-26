package com.malinghan.maconfig.client;

import org.springframework.core.env.EnumerablePropertySource;

import java.util.Map;

public class MaPropertySource extends EnumerablePropertySource<Map<String, String>> {

    private final MaConfigServiceImpl service;

    public MaPropertySource(String name, MaConfigServiceImpl service) {
        super(name, service.getConfigMap());
        this.service = service;
    }

    @Override
    public String[] getPropertyNames() {
        return service.getConfigMap().keySet().toArray(String[]::new);
    }

    @Override
    public Object getProperty(String name) {
        return service.getConfigMap().get(name);
    }
}
