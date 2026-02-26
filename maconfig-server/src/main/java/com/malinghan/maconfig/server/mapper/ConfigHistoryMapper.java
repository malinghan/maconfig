package com.malinghan.maconfig.server.mapper;

import com.malinghan.maconfig.server.model.ConfigHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigHistoryMapper {

    @Insert("INSERT INTO config_history (app, env, ns, pkey, pval, op, created_at) " +
            "VALUES(#{app}, #{env}, #{ns}, #{pkey}, #{pval}, #{op}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ConfigHistory history);

    @Select("<script>" +
            "SELECT * FROM config_history WHERE app=#{app} AND env=#{env} AND ns=#{ns}" +
            "<if test='pkey != null'> AND pkey=#{pkey}</if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    List<ConfigHistory> findHistory(@Param("app") String app, @Param("env") String env,
                                    @Param("ns") String ns, @Param("pkey") String pkey);

    @Select("SELECT * FROM config_history WHERE id=#{id}")
    ConfigHistory findById(@Param("id") long id);
}
