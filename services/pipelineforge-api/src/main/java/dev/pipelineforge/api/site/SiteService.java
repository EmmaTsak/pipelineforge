package dev.pipelineforge.api.site;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

@Service
public class SiteService {

    private final List<SiteResponse> sites =
        new CopyOnWriteArrayList<>(List.of(
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
        ));
    
    public List<SiteResponse> getSites() {
        return List.copyOf(sites);
    }

    public SiteResponse registerSite(String name) {

        String id = UUID.randomUUID().toString();

        SiteResponse newSite = new SiteResponse(
            id,
            name.trim(),
            "UNKNOWN"
        );

        sites.add(newSite);

        return newSite;
    }
}