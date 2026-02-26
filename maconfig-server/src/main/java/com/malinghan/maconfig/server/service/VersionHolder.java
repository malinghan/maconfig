package com.malinghan.maconfig.server.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class VersionHolder {

    private final ConcurrentHashMap<String, Long> versions = new ConcurrentHashMap<>();

    private String key(String app, String env, String ns) {
        return app + ":" + env + ":" + ns;
    }

    public long getVersion(String app, String env, String ns) {
        return versions.getOrDefault(key(app, env, ns), 0L);
    }

    public void updateVersion(String app, String env, String ns) {
        versions.put(key(app, env, ns), System.currentTimeMillis());
    }
}
