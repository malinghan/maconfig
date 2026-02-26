package com.malinghan.maconfig.server.controller;

import com.malinghan.maconfig.server.common.Result;
import com.malinghan.maconfig.server.service.ConfigsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ConfigsController {

    private final ConfigsService configsService;

    @GetMapping("/configs")
    public Result<Map<String, String>> getConfigs(
            @RequestParam String app,
            @RequestParam String env,
            @RequestParam String ns) {
        return Result.ok(configsService.getConfigs(app, env, ns));
    }

    @PostMapping("/configs")
    public Result<Void> saveConfigs(
            @RequestParam String app,
            @RequestParam String env,
            @RequestParam String ns,
            @RequestBody Map<String, String> configs) {
        configsService.saveConfigs(app, env, ns, configs);
        return Result.ok();
    }

    @GetMapping("/version")
    public Result<Long> getVersion(
            @RequestParam String app,
            @RequestParam String env,
            @RequestParam String ns) {
        return Result.ok(configsService.getVersion(app, env, ns));
    }
}
