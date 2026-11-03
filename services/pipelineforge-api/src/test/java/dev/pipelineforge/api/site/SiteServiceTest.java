package dev.pipelineforge.api.site;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SiteServiceTest {
    private final SiteService siteService = new SiteService();

    @Test
    void shouldReturnSampleSites() {

        List<SiteResponse> sites = siteService.getSites();

        assertEquals(2, sites.size());
        assertEquals("cork-01", sites.get(0).id());
        assertEquals("ONLINE", sites.get(0).status());
        assertEquals("dublin-01", sites.get(1).id());
        assertEquals("OFFLINE", sites.get(1).status());
    }

    @Test
    void shouldRegisterNewSite() {
        SiteResponse createdSite =
            siteService.registerSite(" Galway Site 01 ");
        
        assertFalse(createdSite.id().isBlank());

        assertEquals("Galway Site 01", createdSite.name());

        assertEquals("UNKNOWN", createdSite.status());

        assertEquals(3, siteService.getSites().size());
    }
}