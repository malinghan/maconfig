package com.malinghan.maconfig.server.controller;

import com.malinghan.maconfig.server.common.Result;
import com.malinghan.maconfig.server.service.ConfigsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ConfigsController {

    private final ConfigsService configsService;

    @GetMapping("/configs")
    public ResponseEntity<Result<Map<String, String>>> getConfigs(
            @RequestParam String app,
            @RequestParam String env,
            @RequestParam String ns) {
        Map<String, String> configs = configsService.getConfigs(app, env, ns);
        long version = configsService.getVersion(app, env, ns);
        return ResponseEntity.ok()
                .header("X-Version", String.valueOf(version))
                .body(Result.ok(configs));
    }

    @GetMapping("/configs/{pkey}")
    public ResponseEntity<Result<String>> getConfig(
            @RequestParam String app,
            @RequestParam String env,
            @RequestParam String ns,
            @PathVariable String pkey) {
        String val = configsService.getConfig(app, env, ns, pkey);
        if (val == null) {
            return ResponseEntity.ok().body(Result.error(404, "key not found"));
        }
        return ResponseEntity.ok().body(Result.ok(val));
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

    @DeleteMapping("/configs")
    public Result<Void> deleteConfig(
            @RequestParam String app,
            @RequestParam String env,
            @RequestParam String ns,
            @RequestParam String pkey) {
        configsService.deleteConfig(app, env, ns, pkey);
        return Result.ok();
    }

    @GetMapping("/version")
    public Result<Long> getVersion(
            @RequestParam String app,
            @RequestParam String env,
            @RequestParam String ns) {
        return Result.ok(configsService.getVersion(app, env, ns));
    }

    @GetMapping("/apps")
    public Result<List<String>> getAllApps() {
        return Result.ok(configsService.getAllApps());
    }
}