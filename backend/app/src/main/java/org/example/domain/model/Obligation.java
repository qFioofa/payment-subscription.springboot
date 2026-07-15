package org.example.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Obligation(
        UUID id,
        String title,
        BigDecimal amount,
        String currency,
        Category category,
        Recurrence recurrence,
        LocalDate nextPaymentDate,
        Status status,
        Instant createdAt,
        Instant updatedAt
) {
}
