package com.malinghan.maconfig.client;

import com.malinghan.maconfig.client.repository.HttpMaRepository;
import com.malinghan.maconfig.client.repository.MaRepository;
import lombok.Setter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class MaConfigServiceImpl implements ApplicationContextAware, InitializingBean, DisposableBean, RepositoryChangeListener {

    private static final Logger log = Logger.getLogger(MaConfigServiceImpl.class.getName());

    private final ConfigMeta meta;
    private MaRepository repository;
    private volatile Map<String, String> configMap = new ConcurrentHashMap<>();
    private volatile long localVersion = 0L;
    private final List<RepositoryChangeListener> listeners = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;

    @Setter
    private ApplicationContext applicationContext;

    public MaConfigServiceImpl(ConfigMeta meta) {
        this.meta = meta;
    }

    @Override
    public void afterPropertiesSet() {
        this.repository = new HttpMaRepository();
        try {
            Map<String, String> initial = repository.getConfigs(meta);
            configMap = new ConcurrentHashMap<>(initial);
            localVersion = repository.getVersion(meta);
            log.info("MaConfig loaded " + initial.size() + " keys for " + meta.getApp() + "/" + meta.getEnv() + "/" + meta.getNs());
        } catch (Exception e) {
            log.warning("MaConfig initial load failed, will retry in poll: " + e.getMessage());
        }

        listeners.add(this);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "maconfig-poller");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::poll, 5, 5, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            long remoteVersion = repository.getVersion(meta);
            if (remoteVersion != localVersion) {
                Map<String, String> newConfigs = repository.getConfigs(meta);
                Set<String> changedKeys = calcChangedKeys(configMap, newConfigs);
                configMap = new ConcurrentHashMap<>(newConfigs);
                localVersion = remoteVersion;
                log.info("MaConfig detected changes, changed keys: " + changedKeys);
                publishChange(changedKeys);
                for (RepositoryChangeListener listener : listeners) {
                    listener.onChange(newConfigs);
                }
            }
        } catch (Exception e) {
            log.warning("MaConfig poll failed: " + e.getMessage());
        }
    }

    private Set<String> calcChangedKeys(Map<String, String> oldMap, Map<String, String> newMap) {
        Set<String> changed = new HashSet<>();
        for (Map.Entry<String, String> entry : newMap.entrySet()) {
            if (!Objects.equals(oldMap.get(entry.getKey()), entry.getValue())) {
                changed.add(entry.getKey());
            }
        }
        for (String key : oldMap.keySet()) {
            if (!newMap.containsKey(key)) {
                changed.add(key);
            }
        }
        return changed;
    }

    private void publishChange(Set<String> changedKeys) {
        if (applicationContext != null && !changedKeys.isEmpty()) {
            applicationContext.publishEvent(new EnvironmentChangeEvent(applicationContext, changedKeys));
        }
    }

    @Override
    public void onChange(Map<String, String> newConfigs) {
        // self-listener: configMap already updated in poll()
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    @Override
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
