package dev.pipelineforge.api.site;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    @GetMapping
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