package org.example.datasource.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.domain.model.Obligation;
import org.example.domain.model.Payment;

public interface ObligationRepository {

    Obligation save(Obligation obligation);

    Optional<Obligation> findById(UUID id);

    List<Obligation> findAll();

    List<Obligation> findActiveByTitleIgnoreCase(String title);

    List<Obligation> findByNextPaymentDateBetween(LocalDate from, LocalDate to);

    Payment savePayment(Payment payment);

    void deleteById(UUID id);
}
