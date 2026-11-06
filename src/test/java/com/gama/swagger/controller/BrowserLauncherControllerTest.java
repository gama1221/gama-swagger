package com.gama.swagger.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BrowserLauncherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnBrowserLauncherSteps() throws Exception {
        mockMvc.perform(get("/api/browser/steps")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.component").value("BrowserLauncher (CommandLineRunner)"))
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps.length()").value(5))
                .andExpect(jsonPath("$.steps[0].name").value("Check Enablement Flag"))
                .andExpect(jsonPath("$.steps[1].name").value("Operating System Detection"))
                .andExpect(jsonPath("$.steps[2].name").value("OS-Specific Browser Launch Command"))
                .andExpect(jsonPath("$.steps[3].name").value("Desktop API Fallback (fallbackDesktopBrowse)"))
                .andExpect(jsonPath("$.steps[4].name").value("Error Handling and Logging"));
    }

    @Test
    void shouldReturnCustomOpenApiInfo() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("GAMA API Platform"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.info.description").value("Official REST API documentation and developer portal."))
                .andExpect(jsonPath("$.info.contact.name").value("GAMA Support Team"));
    }
}

