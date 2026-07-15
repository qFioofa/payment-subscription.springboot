package org.example.web.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record UpcomingResponse(
        List<ObligationResponse> obligations,
        Map<String, BigDecimal> totals,
        List<ObligationResponse> renewalAlerts
) {
}
