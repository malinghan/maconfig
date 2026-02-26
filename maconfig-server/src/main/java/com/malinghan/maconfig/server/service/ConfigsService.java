package com.malinghan.maconfig.server.service;

import com.malinghan.maconfig.server.mapper.ConfigHistoryMapper;
import com.malinghan.maconfig.server.mapper.ConfigsMapper;
import com.malinghan.maconfig.server.model.ConfigEntry;
import com.malinghan.maconfig.server.model.ConfigHistory;
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
    private final ConfigHistoryMapper historyMapper;

    public Map<String, String> getConfigs(String app, String env, String ns) {
        List<ConfigEntry> entries = configsMapper.findAll(app, env, ns);
        Map<String, String> result = new HashMap<>();
        for (ConfigEntry entry : entries) {
            result.put(entry.getPkey(), entry.getPval());
        }
        return result;
    }

    public Map<String, String> getConfigsWithInherit(String app, String env, String ns, boolean inherit) {
        Map<String, String> result = new HashMap<>();
        if (inherit && !"public".equals(ns)) {
            List<ConfigEntry> publicEntries = configsMapper.findAll(app, env, "public");
            for (ConfigEntry e : publicEntries) {
                result.put(e.getPkey(), e.getPval());
            }
        }
        List<ConfigEntry> entries = configsMapper.findAll(app, env, ns);
        for (ConfigEntry e : entries) {
            result.put(e.getPkey(), e.getPval());
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
            recordHistory(app, env, ns, e.getKey(), e.getValue(), "SET", now);
        }
        log.info("[MACONFIG] saved {} keys for {}/{}/{}", configs.size(), app, env, ns);
    }

    public void deleteConfig(String app, String env, String ns, String pkey) {
        String oldVal = getConfig(app, env, ns, pkey);
        configsMapper.delete(app, env, ns, pkey);
        recordHistory(app, env, ns, pkey, oldVal, "DELETE", System.currentTimeMillis());
        log.info("[MACONFIG] deleted key={} for {}/{}/{}", pkey, app, env, ns);
    }

    public void rollback(long historyId) {
        ConfigHistory h = historyMapper.findById(historyId);
        if (h == null) {
            throw new IllegalArgumentException("history not found: " + historyId);
        }
        if ("DELETE".equals(h.getOp())) {
            configsMapper.delete(h.getApp(), h.getEnv(), h.getNs(), h.getPkey());
        } else {
            ConfigEntry entry = new ConfigEntry();
            entry.setApp(h.getApp());
            entry.setEnv(h.getEnv());
            entry.setNs(h.getNs());
            entry.setPkey(h.getPkey());
            entry.setPval(h.getPval());
            entry.setUpdatedAt(System.currentTimeMillis());
            configsMapper.upsert(entry);
        }
        recordHistory(h.getApp(), h.getEnv(), h.getNs(), h.getPkey(), h.getPval(),
                "ROLLBACK", System.currentTimeMillis());
        log.info("[MACONFIG] rolled back to history id={} key={}", historyId, h.getPkey());
    }

    public List<ConfigHistory> getHistory(String app, String env, String ns, String pkey) {
        return historyMapper.findHistory(app, env, ns, pkey);
    }

    public long getVersion(String app, String env, String ns) {
        return configsMapper.getMaxVersion(app, env, ns);
    }

    public List<String> getAllApps() {
        return configsMapper.findAllApps();
    }

    public Map<String, String> exportConfigs(String app, String env, String ns) {
        return getConfigs(app, env, ns);
    }

    public void importConfigs(String app, String env, String ns,
                              Map<String, String> configs, String conflictPolicy) {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, String> e : configs.entrySet()) {
            String existing = getConfig(app, env, ns, e.getKey());
            if ("skip".equals(conflictPolicy) && existing != null) {
                continue;
            }
            if ("merge".equals(conflictPolicy) && existing != null) {
                // merge: keep existing, skip
                continue;
            }
            // overwrite (default) or key doesn't exist
            ConfigEntry entry = new ConfigEntry();
            entry.setApp(app);
            entry.setEnv(env);
            entry.setNs(ns);
            entry.setPkey(e.getKey());
            entry.setPval(e.getValue());
            entry.setUpdatedAt(now);
            configsMapper.upsert(entry);
            recordHistory(app, env, ns, e.getKey(), e.getValue(), "IMPORT", now);
        }
        log.info("[MACONFIG] imported {} keys for {}/{}/{} policy={}", configs.size(), app, env, ns, conflictPolicy);
    }

    private void recordHistory(String app, String env, String ns, String pkey, String pval,
                                String op, long ts) {
        ConfigHistory h = new ConfigHistory();
        h.setApp(app);
        h.setEnv(env);
        h.setNs(ns);
        h.setPkey(pkey);
        h.setPval(pval);
        h.setOp(op);
        h.setCreatedAt(ts);
        historyMapper.insert(h);
    }
}
