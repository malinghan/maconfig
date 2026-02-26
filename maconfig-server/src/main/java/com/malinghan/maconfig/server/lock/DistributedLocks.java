package com.malinghan.maconfig.server.lock;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLocks {

    private final DataSource dataSource;

    private Connection lockConn;
    private volatile boolean master = false;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "distributed-lock");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    public void start() {
        scheduler.scheduleWithFixedDelay(this::tryLock, 0, 5, TimeUnit.SECONDS);
    }

    private void tryLock() {
        try {
            if (lockConn == null || lockConn.isClosed()) {
                lockConn = dataSource.getConnection();
                lockConn.setAutoCommit(false);
            }
            try (PreparedStatement ps = lockConn.prepareStatement("SELECT id FROM locks WHERE id=1 FOR UPDATE")) {
                ps.executeQuery();
                master = true;
                log.debug("Acquired distributed lock, this node is master");
            }
        } catch (Exception e) {
            log.debug("Failed to acquire distributed lock: {}", e.getMessage());
            master = false;
            if (lockConn != null) {
                try {
                    lockConn.rollback();
                    lockConn.close();
                } catch (Exception ex) {
                    log.warn("Error closing lock connection", ex);
                }
                lockConn = null;
            }
        }
    }

    @PreDestroy
    public void stop() {
        if (lockConn != null) {
            try {
                lockConn.rollback();
                lockConn.close();
            } catch (Exception e) {
                log.warn("Error closing lock connection on shutdown", e);
            }
        }
        scheduler.shutdown();
    }

    public boolean isMaster() {
        return master;
    }
}
