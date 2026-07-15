package org.example.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateObligationResponse(ObligationResponse obligation, String warning) {
}
