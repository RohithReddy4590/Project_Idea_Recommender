package com.projectrecommender.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectrecommender.agent.core.AgentOrchestrator;
import com.projectrecommender.agent.core.StateManager;
import com.projectrecommender.api.controller.AgentController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for AgentController.
 */
@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgentOrchestrator agentOrchestrator;

    @MockBean
    private StateManager stateManager;

    @Test
    @DisplayName("GET /agent/health returns UP status")
    @WithMockUser
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/agent/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("POST /agent/chat returns agent response")
    @WithMockUser
    void chat_returnsAgentResponse() throws Exception {
        StateManager.AgentState mockState = new StateManager.AgentState("test-session");
        when(stateManager.getOrCreateState("test-session")).thenReturn(mockState);
        when(agentOrchestrator.handleChatMessage(anyString(), anyLong(), anyString()))
                .thenReturn("You should build the Finance Tracker API!");

        Map<String, Object> payload = Map.of(
                "sessionId", "test-session",
                "studentId", 1,
                "message", "What project should I build?"
        );

        mockMvc.perform(post("/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("You should build the Finance Tracker API!"))
                .andExpect(jsonPath("$.sessionId").value("test-session"));
    }

    @Test
    @DisplayName("DELETE /agent/session/{id} clears the session")
    @WithMockUser
    void clearSession_clearsAndReturnsMessage() throws Exception {
        doNothing().when(stateManager).clearSession("abc123");

        mockMvc.perform(delete("/agent/session/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(stateManager, times(1)).clearSession("abc123");
    }
}
