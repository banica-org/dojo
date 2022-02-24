package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.git.GitManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RequestReceiver.class)
public class RequestReceiverIntegrationTest {

    @MockBean
    private GitManager gitManager;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void deleteAllRepositoriesForAGame() throws Exception{
        String gitManagerResponse = "some response";
        when(gitManager.deleteReposForParticularGame(eq("kata"))).thenReturn(gitManagerResponse);
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:8081/repository/kata"))
                .andExpect(jsonPath("$").value(gitManagerResponse))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void deleteAllRepositoriesDenied() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:8081/repository/kata"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
