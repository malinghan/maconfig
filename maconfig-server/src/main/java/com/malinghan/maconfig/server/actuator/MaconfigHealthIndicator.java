package com.malinghan.maconfig.server.actuator;

import com.malinghan.maconfig.server.lock.DistributedLocks;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component("maconfig")
@RequiredArgsConstructor
public class MaconfigHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final DistributedLocks distributedLocks;

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            boolean dbOk = conn.isValid(1);
            return Health.up()
                    .withDetail("db", dbOk ? "ok" : "unreachable")
                    .withDetail("master", distributedLocks.isMaster())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("db", "unreachable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
