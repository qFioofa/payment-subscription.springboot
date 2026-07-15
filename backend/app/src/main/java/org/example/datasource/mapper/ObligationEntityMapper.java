package org.example.datasource.mapper;

import org.example.datasource.model.ObligationEntity;
import org.example.domain.model.Obligation;

public class ObligationEntityMapper {

    public Obligation toDomain(ObligationEntity entity) {
        return new Obligation(
                entity.getId(),
                entity.getTitle(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getCategory(),
                entity.getRecurrence(),
                entity.getNextPaymentDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ObligationEntity toEntity(Obligation obligation) {
        ObligationEntity entity = new ObligationEntity();
        entity.setId(obligation.id());
        entity.setTitle(obligation.title());
        entity.setAmount(obligation.amount());
        entity.setCurrency(obligation.currency());
        entity.setCategory(obligation.category());
        entity.setRecurrence(obligation.recurrence());
        entity.setNextPaymentDate(obligation.nextPaymentDate());
        entity.setStatus(obligation.status());
        entity.setCreatedAt(obligation.createdAt());
        entity.setUpdatedAt(obligation.updatedAt());
        return entity;
    }
}
