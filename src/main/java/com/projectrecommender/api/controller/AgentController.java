package com.projectrecommender.api.controller;

import com.projectrecommender.agent.core.AgentOrchestrator;
import com.projectrecommender.agent.core.StateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for agent chat interactions.
 * Handles conversational AI requests and session management.
 */
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final AgentOrchestrator agentOrchestrator;
    private final StateManager stateManager;

    /**
     * POST /agent/chat
     * Send a message to the agent within an existing session.
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");
        Long studentId   = Long.valueOf(payload.get("studentId").toString());
        String message   = (String) payload.get("message");

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "session-" + studentId + "-" + System.currentTimeMillis();
        }

        log.info("Chat request: sessionId={}, studentId={}", sessionId, studentId);

        String response = agentOrchestrator.handleChatMessage(sessionId, studentId, message);

        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "response", response,
            "interactionCount", stateManager.getOrCreateState(sessionId).getInteractionCount()
        ));
    }

    /**
     * GET /agent/session/{sessionId}
     * Get the current state of an agent session.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
        StateManager.AgentState state = stateManager.getOrCreateState(sessionId);
        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "interactionCount", state.getInteractionCount(),
            "messageCount", state.getMessages().size(),
            "hasProfile", state.getProfileAnalysis() != null
        ));
    }

    /**
     * DELETE /agent/session/{sessionId}
     * Clear an agent session.
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        stateManager.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session cleared: " + sessionId));
    }

    /**
     * GET /agent/health
     * Simple health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "agent", "Project Idea Recommender Agent"));
    }
}
