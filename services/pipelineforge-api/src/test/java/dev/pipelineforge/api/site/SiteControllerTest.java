
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;

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

    
    @Test
    void shouldRegisterSite() throws Exception {

        SiteResponse created = new SiteResponse(
            "galway-01",
            "Galway Site 01",
            "UNKNOWN"
        );

        when(siteService.registerSite("Galway Site 01"))
            .thenReturn(created);

        mockMvc.perform(post("/api/sites")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Galway Site 01"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("galway-01"))
            .andExpect(jsonPath("$.name").value("Galway Site 01"))
            .andExpect(jsonPath("$.status").value("UNKNOWN"));
    }

    @Test
    void shouldRejectBlankSiteName() throws Exception {

        mockMvc.perform(post("/api/sites")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": ""}
                    """))
            .andExpect(status().isBadRequest());
    }

}
