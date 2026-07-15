package org.example.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record UpcomingObligations(
        List<Obligation> obligations,
        Map<String, BigDecimal> totals,
        List<Obligation> renewalAlerts
) {
}
