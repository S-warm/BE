package com.swarm.dashboard.controller;

import com.swarm.dashboard.service.StagingPayloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/simulations/{projectId}")
@RequiredArgsConstructor
public class AiCallbackController {

    private final StagingPayloadService stagingService;

    @PostMapping("/overview")
    public ResponseEntity<Void> overview(@PathVariable UUID projectId,
                                         @RequestBody String rawJson) {
        stagingService.receive(projectId, "overview", rawJson);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/issues")
    public ResponseEntity<Void> issues(@PathVariable UUID projectId,
                                       @RequestBody String rawJson) {
        stagingService.receive(projectId, "issues", rawJson);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/heatmap")
    public ResponseEntity<Void> heatmap(@PathVariable UUID projectId,
                                        @RequestBody String rawJson) {
        stagingService.receive(projectId, "heatmap", rawJson);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/wcag")
    public ResponseEntity<Void> wcag(@PathVariable UUID projectId,
                                     @RequestBody String rawJson) {
        stagingService.receive(projectId, "wcag", rawJson);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fixes")
    public ResponseEntity<Void> fixes(@PathVariable UUID projectId,
                                      @RequestBody String rawJson) {
        stagingService.receive(projectId, "fixes", rawJson);
        return ResponseEntity.ok().build();
    }
}
