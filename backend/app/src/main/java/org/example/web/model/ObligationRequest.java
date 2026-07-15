package org.example.web.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.domain.model.Category;
import org.example.domain.model.Recurrence;

public record ObligationRequest(
        @NotBlank String title,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @NotNull Category category,
        Recurrence recurrence,
        @NotNull LocalDate nextPaymentDate
) {
}
