package org.example.web.mapper;

import org.example.domain.model.Payment;
import org.example.web.model.PaymentResponse;

public class PaymentWebMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.id(),
                payment.obligationId(),
                payment.amount(),
                payment.currency(),
                payment.paidAt()
        );
    }
}
