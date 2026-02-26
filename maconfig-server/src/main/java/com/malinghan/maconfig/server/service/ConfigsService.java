package com.malinghan.maconfig.server.service;

import com.malinghan.maconfig.server.mapper.ConfigsMapper;
import com.malinghan.maconfig.server.model.ConfigEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigsService {

    private final ConfigsMapper configsMapper;
    private final VersionHolder versionHolder;

    public Map<String, String> getConfigs(String app, String env, String ns) {
        List<ConfigEntry> entries = configsMapper.findAll(app, env, ns);
        Map<String, String> result = new HashMap<>();
        for (ConfigEntry entry : entries) {
            result.put(entry.getPkey(), entry.getPval());
        }
        return result;
    }

    public void saveConfigs(String app, String env, String ns, Map<String, String> configs) {
        for (Map.Entry<String, String> e : configs.entrySet()) {
            ConfigEntry entry = new ConfigEntry();
            entry.setApp(app);
            entry.setEnv(env);
            entry.setNs(ns);
            entry.setPkey(e.getKey());
            entry.setPval(e.getValue());
            configsMapper.upsert(entry);
        }
        versionHolder.updateVersion(app, env, ns);
    }

    public long getVersion(String app, String env, String ns) {
        return versionHolder.getVersion(app, env, ns);
    }
}
