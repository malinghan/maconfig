package com.malinghan.maconfig.server.service;

import com.malinghan.maconfig.server.mapper.ConfigsMapper;
import com.malinghan.maconfig.server.model.ConfigEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigsService {

    private final ConfigsMapper configsMapper;

    public Map<String, String> getConfigs(String app, String env, String ns) {
        List<ConfigEntry> entries = configsMapper.findAll(app, env, ns);
        Map<String, String> result = new HashMap<>();
        for (ConfigEntry entry : entries) {
            result.put(entry.getPkey(), entry.getPval());
        }
        return result;
    }

    public String getConfig(String app, String env, String ns, String pkey) {
        ConfigEntry entry = configsMapper.findOne(app, env, ns, pkey);
        return entry == null ? null : entry.getPval();
    }

    public void saveConfigs(String app, String env, String ns, Map<String, String> configs) {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, String> e : configs.entrySet()) {
            ConfigEntry entry = new ConfigEntry();
            entry.setApp(app);
            entry.setEnv(env);
            entry.setNs(ns);
            entry.setPkey(e.getKey());
            entry.setPval(e.getValue());
            entry.setUpdatedAt(now);
            configsMapper.upsert(entry);
        }
        log.info("[MACONFIG] saved {} keys for {}/{}/{}", configs.size(), app, env, ns);
    }

    public void deleteConfig(String app, String env, String ns, String pkey) {
        configsMapper.delete(app, env, ns, pkey);
        log.info("[MACONFIG] deleted key={} for {}/{}/{}", pkey, app, env, ns);
    }

    public long getVersion(String app, String env, String ns) {
        return configsMapper.getMaxVersion(app, env, ns);
    }

    public List<String> getAllApps() {
        return configsMapper.findAllApps();
    }
}