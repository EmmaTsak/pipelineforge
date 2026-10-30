package dev.pipelineforge.api.site;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SiteService {

    public List<SiteResponse> getSites() {
        return List.of(
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
        );
    }
}