package org.example.datasource.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.example.datasource.model.ObligationEntity;
import org.example.domain.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObligationJpaRepository extends JpaRepository<ObligationEntity, UUID> {

    List<ObligationEntity> findByTitleIgnoreCaseAndStatus(String title, Status status);

    List<ObligationEntity> findByNextPaymentDateBetween(LocalDate from, LocalDate to);
}
