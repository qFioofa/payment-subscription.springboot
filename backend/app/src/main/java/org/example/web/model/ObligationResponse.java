package org.example.web.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.example.domain.model.Category;
import org.example.domain.model.Recurrence;
import org.example.domain.model.Status;

public record ObligationResponse(
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
