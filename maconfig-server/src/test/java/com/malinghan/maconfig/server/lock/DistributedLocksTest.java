package com.malinghan.maconfig.server.lock;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DistributedLocksTest {

    @Test
    void isMaster_false_before_start() {
        DataSource ds = mock(DataSource.class);
        DistributedLocks locks = new DistributedLocks(ds);
        assertThat(locks.isMaster()).isFalse();
    }

    @Test
    void becomes_master_when_lock_acquired() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(conn);
        when(conn.isClosed()).thenReturn(false);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        DistributedLocks locks = new DistributedLocks(ds);
        locks.start();
        // give the scheduler a moment to run
        Thread.sleep(200);

        assertThat(locks.isMaster()).isTrue();
        locks.stop();
    }

    @Test
    void remains_standby_when_lock_fails() throws Exception {
        DataSource ds = mock(DataSource.class);
        when(ds.getConnection()).thenThrow(new RuntimeException("connection refused"));

        DistributedLocks locks = new DistributedLocks(ds);
        locks.start();
        Thread.sleep(200);

        assertThat(locks.isMaster()).isFalse();
        locks.stop();
    }
}
