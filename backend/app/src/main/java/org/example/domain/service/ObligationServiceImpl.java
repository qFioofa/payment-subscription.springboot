package org.example.domain.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.datasource.repository.ObligationRepository;
import org.example.domain.exception.InvalidObligationStateException;
import org.example.domain.exception.ObligationNotFoundException;
import org.example.domain.model.Category;
import org.example.domain.model.CreatedObligation;
import org.example.domain.model.Obligation;
import org.example.domain.model.PaidObligation;
import org.example.domain.model.Payment;
import org.example.domain.model.Status;
import org.example.domain.model.UpcomingObligations;

public class ObligationServiceImpl implements ObligationService {

    private final ObligationRepository repository;

    public ObligationServiceImpl(ObligationRepository repository) {
        this.repository = repository;
    }

    @Override
    public CreatedObligation create(Obligation obligation) {
        Instant now = Instant.now();
        Status status = obligation.nextPaymentDate().isBefore(LocalDate.now())
                ? Status.EXPIRED
                : Status.ACTIVE;
        Obligation toSave = new Obligation(
                UUID.randomUUID(), obligation.title(), obligation.amount(), obligation.currency(),
                obligation.category(), obligation.recurrence(), obligation.nextPaymentDate(),
                status, now, now);
        String warning = repository.findActiveByTitleIgnoreCase(obligation.title()).isEmpty()
                ? null
                : "Активное обязательство с таким названием уже существует";
        return new CreatedObligation(repository.save(toSave), warning);
    }

    @Override
    public List<Obligation> findAll(Category category, Status status) {
        LocalDate today = LocalDate.now();
        return repository.findAll().stream()
                .map(o -> expiresLazily(o, today) ? repository.save(expire(o)) : o)
                .filter(o -> category == null || o.category() == category)
                .filter(o -> status == null || o.status() == status)
                .sorted(Comparator.comparing(Obligation::nextPaymentDate))
                .toList();
    }

    @Override
    public UpcomingObligations findUpcoming(int days) {
        LocalDate today = LocalDate.now();
        List<Obligation> window = repository.findByNextPaymentDateBetween(today, today.plusDays(days)).stream()
                .sorted(Comparator.comparing(Obligation::nextPaymentDate))
                .toList();
        Map<String, BigDecimal> totals = window.stream().collect(Collectors.groupingBy(
                Obligation::currency,
                Collectors.reducing(BigDecimal.ZERO, Obligation::amount, BigDecimal::add)));
        List<Obligation> renewalAlerts = window.stream()
                .filter(o -> o.category() == Category.SUBSCRIPTION && o.recurrence() != null)
                .toList();
        return new UpcomingObligations(window, totals, renewalAlerts);
    }

    private static boolean expiresLazily(Obligation o, LocalDate today) {
        return o.status() == Status.ACTIVE
                && o.recurrence() == null
                && o.nextPaymentDate().isBefore(today);
    }

    private static Obligation expire(Obligation o) {
        return new Obligation(o.id(), o.title(), o.amount(), o.currency(), o.category(),
                o.recurrence(), o.nextPaymentDate(), Status.EXPIRED, o.createdAt(), Instant.now());
    }

    @Override
    public PaidObligation pay(UUID id) {
        Obligation o = requireActive(id, "Оплатить можно только активное обязательство");
        Instant now = Instant.now();
        Payment payment = repository.savePayment(new Payment(
                UUID.randomUUID(), o.id(), o.amount(), o.currency(), now));
        return new PaidObligation(repository.save(advance(o, now)), payment);
    }

    @Override
    public Obligation cancel(UUID id) {
        Obligation o = requireActive(id, "Отменить можно только активное обязательство");
        return repository.save(new Obligation(o.id(), o.title(), o.amount(), o.currency(),
                o.category(), o.recurrence(), o.nextPaymentDate(), Status.CANCELLED,
                o.createdAt(), Instant.now()));
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private Obligation requireActive(UUID id, String message) {
        Obligation o = repository.findById(id).orElseThrow(() -> new ObligationNotFoundException(id));
        if (o.status() != Status.ACTIVE) {
            throw new InvalidObligationStateException(message);
        }
        return o;
    }

    private static Obligation advance(Obligation o, Instant now) {
        if (o.recurrence() == null) {
            return new Obligation(o.id(), o.title(), o.amount(), o.currency(), o.category(),
                    null, o.nextPaymentDate(), Status.CANCELLED, o.createdAt(), now);
        }
        LocalDate next = switch (o.recurrence()) {
            case MONTHLY -> o.nextPaymentDate().plusMonths(1);
            case QUARTERLY -> o.nextPaymentDate().plusMonths(3);
            case YEARLY -> o.nextPaymentDate().plusYears(1);
        };
        return new Obligation(o.id(), o.title(), o.amount(), o.currency(), o.category(),
                o.recurrence(), next, Status.ACTIVE, o.createdAt(), now);
    }
}
