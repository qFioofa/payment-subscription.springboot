package org.example.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payment(
        UUID id,
        UUID obligationId,
        BigDecimal amount,
        String currency,
        Instant paidAt
) {
}
