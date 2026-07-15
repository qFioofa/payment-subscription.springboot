package org.example.web.mapper;

import org.example.domain.model.CreatedObligation;
import org.example.domain.model.Obligation;
import org.example.domain.model.UpcomingObligations;
import org.example.web.model.CreateObligationResponse;
import org.example.web.model.ObligationRequest;
import org.example.web.model.ObligationResponse;
import org.example.web.model.UpcomingResponse;

public class ObligationWebMapper {

    public Obligation toDomain(ObligationRequest request) {
        return new Obligation(
                null,
                request.title(),
                request.amount(),
                request.currency(),
                request.category(),
                request.recurrence(),
                request.nextPaymentDate(),
                null,
                null,
                null
        );
    }

    public ObligationResponse toResponse(Obligation obligation) {
        return new ObligationResponse(
                obligation.id(),
                obligation.title(),
                obligation.amount(),
                obligation.currency(),
                obligation.category(),
                obligation.recurrence(),
                obligation.nextPaymentDate(),
                obligation.status(),
                obligation.createdAt(),
                obligation.updatedAt()
        );
    }

    public CreateObligationResponse toResponse(CreatedObligation created) {
        return new CreateObligationResponse(toResponse(created.obligation()), created.warning());
    }

    public UpcomingResponse toResponse(UpcomingObligations upcoming) {
        return new UpcomingResponse(
                upcoming.obligations().stream().map(this::toResponse).toList(),
                upcoming.totals(),
                upcoming.renewalAlerts().stream().map(this::toResponse).toList()
        );
    }
}
