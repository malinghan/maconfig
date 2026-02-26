package com.malinghan.maconfig.server.controller;

import com.malinghan.maconfig.server.common.Result;
import com.malinghan.maconfig.server.lock.DistributedLocks;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class StatusController {

    private final DistributedLocks distributedLocks;

    @GetMapping("/status")
    public Result<Boolean> status() {
        return Result.ok(distributedLocks.isMaster());
    }
}
