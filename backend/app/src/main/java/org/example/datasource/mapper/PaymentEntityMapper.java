package org.example.datasource.mapper;

import org.example.datasource.model.PaymentEntity;
import org.example.domain.model.Payment;

public class PaymentEntityMapper {

    public Payment toDomain(PaymentEntity entity) {
        return new Payment(
                entity.getId(),
                entity.getObligationId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getPaidAt()
        );
    }

    public PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.id());
        entity.setObligationId(payment.obligationId());
        entity.setAmount(payment.amount());
        entity.setCurrency(payment.currency());
        entity.setPaidAt(payment.paidAt());
        return entity;
    }
}
