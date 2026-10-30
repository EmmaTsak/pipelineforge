
package dev.pipelineforge.api.site;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SiteController.class)
class SiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteService siteService;

    @Test
    void shouldReturnMonitoringSites() throws Exception {

        when(siteService.getSites()).thenReturn(
            List.of(
                new SiteResponse(
                    "cork-01",
                    "Cork Site 01",
                    "ONLINE"
                ),
                new SiteResponse(
                    "dublin-01",
                    "Dublin Site 01",
                    "OFFLINE"
                )
            )
        );

        mockMvc.perform(get("/api/sites"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("cork-01"))
            .andExpect(jsonPath("$[0].status").value("ONLINE"))
            .andExpect(jsonPath("$[1].status").value("OFFLINE"));
    }
}
