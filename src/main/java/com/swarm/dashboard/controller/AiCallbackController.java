package com.swarm.dashboard.controller;

import com.swarm.dashboard.service.S3ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/simulations/{projectId}")
@RequiredArgsConstructor
public class AiCallbackController {

    private final S3ResultService s3ResultService;

    @PostMapping("/complete")
    public ResponseEntity<Void> complete(@PathVariable UUID projectId) {
        CompletableFuture.runAsync(() -> s3ResultService.processFromS3(projectId));
        return ResponseEntity.ok().build();
    }
}
