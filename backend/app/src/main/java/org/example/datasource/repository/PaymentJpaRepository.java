package org.example.datasource.repository;

import java.util.UUID;
import org.example.datasource.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    void deleteByObligationId(UUID obligationId);
}
