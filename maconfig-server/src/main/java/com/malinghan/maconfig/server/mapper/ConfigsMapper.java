package com.malinghan.maconfig.server.mapper;

import com.malinghan.maconfig.server.model.ConfigEntry;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigsMapper {

    @Select("SELECT * FROM configs WHERE app=#{app} AND env=#{env} AND ns=#{ns}")
    List<ConfigEntry> findAll(@Param("app") String app, @Param("env") String env, @Param("ns") String ns);

    @Insert("MERGE INTO configs (app, env, ns, pkey, pval) KEY(app, env, ns, pkey) VALUES(#{app}, #{env}, #{ns}, #{pkey}, #{pval})")
    void upsert(ConfigEntry entry);

    @Delete("DELETE FROM configs WHERE app=#{app} AND env=#{env} AND ns=#{ns} AND pkey=#{pkey}")
    void delete(@Param("app") String app, @Param("env") String env, @Param("ns") String ns, @Param("pkey") String pkey);
}
