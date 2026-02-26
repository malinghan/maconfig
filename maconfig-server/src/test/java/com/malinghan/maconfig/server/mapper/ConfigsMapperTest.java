package com.malinghan.maconfig.server.mapper;

import com.malinghan.maconfig.server.model.ConfigEntry;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql("/schema.sql")
class ConfigsMapperTest {

    @Autowired
    ConfigsMapper mapper;

    @Test
    void upsert_and_findAll() {
        ConfigEntry e = entry("app1", "dev", "ns1", "key1", "val1");
        mapper.upsert(e);

        List<ConfigEntry> list = mapper.findAll("app1", "dev", "ns1");
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPval()).isEqualTo("val1");
    }

    @Test
    void upsert_updates_existing_key() {
        mapper.upsert(entry("app1", "dev", "ns1", "key1", "v1"));
        mapper.upsert(entry("app1", "dev", "ns1", "key1", "v2"));

        List<ConfigEntry> list = mapper.findAll("app1", "dev", "ns1");
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPval()).isEqualTo("v2");
    }

    @Test
    void delete_removes_key() {
        mapper.upsert(entry("app1", "dev", "ns1", "key1", "val1"));
        mapper.delete("app1", "dev", "ns1", "key1");

        assertThat(mapper.findAll("app1", "dev", "ns1")).isEmpty();
    }

    @Test
    void getMaxVersion_returns_zero_when_empty() {
        long version = mapper.getMaxVersion("no-app", "dev", "ns1");
        assertThat(version).isEqualTo(0L);
    }

    @Test
    void getMaxVersion_returns_max_updated_at() {
        ConfigEntry e1 = entry("app1", "dev", "ns1", "k1", "v1");
        e1.setUpdatedAt(1000L);
        ConfigEntry e2 = entry("app1", "dev", "ns1", "k2", "v2");
        e2.setUpdatedAt(2000L);
        mapper.upsert(e1);
        mapper.upsert(e2);

        assertThat(mapper.getMaxVersion("app1", "dev", "ns1")).isEqualTo(2000L);
    }

    @Test
    void findOne_returns_null_for_missing_key() {
        assertThat(mapper.findOne("app1", "dev", "ns1", "missing")).isNull();
    }

    @Test
    void findAllApps_returns_distinct_apps() {
        mapper.upsert(entry("appA", "dev", "ns1", "k1", "v1"));
        mapper.upsert(entry("appB", "dev", "ns1", "k1", "v1"));
        mapper.upsert(entry("appA", "dev", "ns1", "k2", "v2"));

        List<String> apps = mapper.findAllApps();
        assertThat(apps).containsExactlyInAnyOrder("appA", "appB");
    }

    private ConfigEntry entry(String app, String env, String ns, String pkey, String pval) {
        ConfigEntry e = new ConfigEntry();
        e.setApp(app);
        e.setEnv(env);
        e.setNs(ns);
        e.setPkey(pkey);
        e.setPval(pval);
        e.setUpdatedAt(System.currentTimeMillis());
        return e;
    }
}
