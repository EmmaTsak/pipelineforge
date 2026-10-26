package dev.pipelineforge.api.site;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SiteController.class)
class SiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnMonitoringSites() throws Exception {
        mockMvc.perform(get("/api/sites"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("cork-01"))
            .andExpect(jsonPath("$[0].name").value("Cork Site 01"))
            .andExpect(jsonPath("$[0].status").value("ONLINE"))
            .andExpect(jsonPath("$[1].id").value("dublin-01"))
            .andExpect(jsonPath("$[1].status").value("OFFLINE"));
    }
}