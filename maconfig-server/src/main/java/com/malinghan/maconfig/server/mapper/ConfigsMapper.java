package com.malinghan.maconfig.server.mapper;

import com.malinghan.maconfig.server.model.ConfigEntry;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigsMapper {

    @Select("SELECT * FROM configs WHERE app=#{app} AND env=#{env} AND ns=#{ns}")
    List<ConfigEntry> findAll(@Param("app") String app, @Param("env") String env, @Param("ns") String ns);

    @Insert("MERGE INTO configs (app, env, ns, pkey, pval, updated_at) KEY(app, env, ns, pkey) VALUES(#{app}, #{env}, #{ns}, #{pkey}, #{pval}, #{updatedAt})")
    void upsert(ConfigEntry entry);

    @Delete("DELETE FROM configs WHERE app=#{app} AND env=#{env} AND ns=#{ns} AND pkey=#{pkey}")
    void delete(@Param("app") String app, @Param("env") String env, @Param("ns") String ns, @Param("pkey") String pkey);

    @Select("SELECT COALESCE(MAX(updated_at), 0) FROM configs WHERE app=#{app} AND env=#{env} AND ns=#{ns}")
    long getMaxVersion(@Param("app") String app, @Param("env") String env, @Param("ns") String ns);

    @Select("SELECT DISTINCT app FROM configs")
    List<String> findAllApps();

    @Select("SELECT * FROM configs WHERE app=#{app} AND env=#{env} AND ns=#{ns} AND pkey=#{pkey}")
    ConfigEntry findOne(@Param("app") String app, @Param("env") String env, @Param("ns") String ns, @Param("pkey") String pkey);
}
