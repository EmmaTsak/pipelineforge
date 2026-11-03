package dev.pipelineforge.api.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSiteRequest(
    @NotBlank
    @Size(max = 80)
    String name
) {   
}