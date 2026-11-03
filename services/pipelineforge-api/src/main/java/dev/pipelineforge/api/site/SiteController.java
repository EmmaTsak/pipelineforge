package dev.pipelineforge.api.site;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public List<SiteResponse> getSites() {
        return siteService.getSites();
    }

    @PostMapping
    public ResponseEntity<SiteResponse> registerSite(
        @Valid @RequestBody CreateSiteRequest request) {
            SiteResponse createdSite = siteService.registerSite(request.name());
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdSite);
    }
}

