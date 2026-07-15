package org.example.datasource.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.datasource.mapper.ObligationEntityMapper;
import org.example.datasource.mapper.PaymentEntityMapper;
import org.example.domain.model.Obligation;
import org.example.domain.model.Payment;
import org.example.domain.model.Status;

public class ObligationRepositoryImpl implements ObligationRepository {

    private final ObligationJpaRepository obligations;
    private final PaymentJpaRepository payments;
    private final ObligationEntityMapper obligationMapper;
    private final PaymentEntityMapper paymentMapper;

    public ObligationRepositoryImpl(
            ObligationJpaRepository obligations,
            PaymentJpaRepository payments,
            ObligationEntityMapper obligationMapper,
            PaymentEntityMapper paymentMapper
    ) {
        this.obligations = obligations;
        this.payments = payments;
        this.obligationMapper = obligationMapper;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public Obligation save(Obligation obligation) {
        return obligationMapper.toDomain(obligations.save(obligationMapper.toEntity(obligation)));
    }

    @Override
    public Optional<Obligation> findById(UUID id) {
        return obligations.findById(id).map(obligationMapper::toDomain);
    }

    @Override
    public List<Obligation> findAll() {
        return obligations.findAll().stream().map(obligationMapper::toDomain).toList();
    }

    @Override
    public List<Obligation> findActiveByTitleIgnoreCase(String title) {
        return obligations.findByTitleIgnoreCaseAndStatus(title, Status.ACTIVE).stream()
                .map(obligationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Obligation> findByNextPaymentDateBetween(LocalDate from, LocalDate to) {
        return obligations.findByNextPaymentDateBetween(from, to).stream()
                .map(obligationMapper::toDomain)
                .toList();
    }

    @Override
    public Payment savePayment(Payment payment) {
        return paymentMapper.toDomain(payments.save(paymentMapper.toEntity(payment)));
    }

    @Override
    public void deleteById(UUID id) {
        payments.deleteByObligationId(id);
        obligations.deleteById(id);
    }
}
