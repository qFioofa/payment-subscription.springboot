package org.example.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.datasource.repository.ObligationRepository;
import org.example.domain.exception.InvalidObligationStateException;
import org.example.domain.exception.ObligationNotFoundException;
import org.example.domain.model.Category;
import org.example.domain.model.CreatedObligation;
import org.example.domain.model.Obligation;
import org.example.domain.model.PaidObligation;
import org.example.domain.model.Payment;
import org.example.domain.model.Recurrence;
import org.example.domain.model.Status;
import org.example.domain.model.UpcomingObligations;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ObligationServiceImplTest {

    private final ObligationRepository repository = org.mockito.Mockito.mock(ObligationRepository.class);
    private final ObligationServiceImpl service = new ObligationServiceImpl(repository);

    private static Obligation obligation(Category category, Recurrence recurrence, LocalDate next, Status status) {
        return new Obligation(UUID.randomUUID(), "Netflix", new BigDecimal("9.99"), "USD",
                category, recurrence, next, status, Instant.EPOCH, Instant.EPOCH);
    }

    @Test
    void lazyExpiryExpiresOverdueOneOffButLeavesRecurrentActive() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Obligation oneOff = obligation(Category.BILL, null, yesterday, Status.ACTIVE);
        Obligation recurrent = obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY, yesterday, Status.ACTIVE);
        when(repository.findAll()).thenReturn(List.of(oneOff, recurrent));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Obligation> result = service.findAll(null, null);

        ArgumentCaptor<Obligation> saved = ArgumentCaptor.forClass(Obligation.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().id()).isEqualTo(oneOff.id());
        assertThat(saved.getValue().status()).isEqualTo(Status.EXPIRED);

        assertThat(result).extracting(Obligation::id, Obligation::status)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(oneOff.id(), Status.EXPIRED),
                        org.assertj.core.groups.Tuple.tuple(recurrent.id(), Status.ACTIVE));
    }

    @Test
    void lazyExpiryDoesNotTouchFutureDatedActive() {
        Obligation future = obligation(Category.BILL, null, LocalDate.now().plusDays(1), Status.ACTIVE);
        when(repository.findAll()).thenReturn(List.of(future));

        service.findAll(null, null);

        verify(repository, never()).save(any());
    }

    @Test
    void findAllAppliesCategoryAndStatusFiltersAndSortsByNextPaymentDate() {
        Obligation later = obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY, LocalDate.now().plusDays(10), Status.ACTIVE);
        Obligation sooner = obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY, LocalDate.now().plusDays(2), Status.ACTIVE);
        Obligation otherCategory = obligation(Category.BILL, null, LocalDate.now().plusDays(1), Status.ACTIVE);
        when(repository.findAll()).thenReturn(List.of(later, otherCategory, sooner));

        List<Obligation> result = service.findAll(Category.SUBSCRIPTION, Status.ACTIVE);

        assertThat(result).extracting(Obligation::id).containsExactly(sooner.id(), later.id());
    }

    @Test
    void upcomingSumsTotalsByCurrencyAndAlertsOnlyRecurrentSubscriptions() {
        LocalDate today = LocalDate.now();
        Obligation sub = obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY, today.plusDays(1), Status.ACTIVE);
        Obligation bill = new Obligation(UUID.randomUUID(), "Rent", new BigDecimal("1490.00"), "RUB",
                Category.BILL, null, today.plusDays(2), Status.ACTIVE, Instant.EPOCH, Instant.EPOCH);
        when(repository.findByNextPaymentDateBetween(today, today.plusDays(7)))
                .thenReturn(List.of(bill, sub));

        UpcomingObligations result = service.findUpcoming(7);

        assertThat(result.obligations()).extracting(Obligation::id).containsExactly(sub.id(), bill.id());
        assertThat(result.totals()).containsOnly(
                org.assertj.core.api.Assertions.entry("USD", new BigDecimal("9.99")),
                org.assertj.core.api.Assertions.entry("RUB", new BigDecimal("1490.00")));
        assertThat(result.renewalAlerts()).extracting(Obligation::id).containsExactly(sub.id());
    }

    @Test
    void createInThePastGetsExpiredStatus() {
        Obligation input = obligation(Category.BILL, null, LocalDate.now().minusDays(1), null);
        when(repository.findActiveByTitleIgnoreCase(any())).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatedObligation created = service.create(input);

        assertThat(created.obligation().status()).isEqualTo(Status.EXPIRED);
        assertThat(created.warning()).isNull();
    }

    @Test
    void createDuplicateActiveTitleReturnsWarning() {
        Obligation input = obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY, LocalDate.now().plusDays(5), null);
        when(repository.findActiveByTitleIgnoreCase("Netflix"))
                .thenReturn(List.of(obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY, LocalDate.now().plusDays(5), Status.ACTIVE)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatedObligation created = service.create(input);

        assertThat(created.obligation().status()).isEqualTo(Status.ACTIVE);
        assertThat(created.warning()).isNotBlank();
    }

    @Test
    void payMonthlyAdvancesOneMonthKeepsActiveAndRecordsPayment() {
        Obligation saved = payReturns(obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY,
                LocalDate.of(2025, 3, 15), Status.ACTIVE));

        assertThat(saved.nextPaymentDate()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(saved.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void payQuarterlyAdvancesThreeMonths() {
        Obligation saved = payReturns(obligation(Category.SUBSCRIPTION, Recurrence.QUARTERLY,
                LocalDate.of(2025, 1, 15), Status.ACTIVE));
        assertThat(saved.nextPaymentDate()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(saved.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void payYearlyAdvancesOneYear() {
        Obligation saved = payReturns(obligation(Category.SUBSCRIPTION, Recurrence.YEARLY,
                LocalDate.of(2025, 1, 15), Status.ACTIVE));
        assertThat(saved.nextPaymentDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(saved.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void payOneOffCancels() {
        Obligation saved = payReturns(obligation(Category.BILL, null,
                LocalDate.of(2025, 1, 15), Status.ACTIVE));
        assertThat(saved.status()).isEqualTo(Status.CANCELLED);
        assertThat(saved.nextPaymentDate()).isEqualTo(LocalDate.of(2025, 1, 15));
    }

    @Test
    void payOnJan31MonthlyLandsOnEndOfFebruaryWithoutError() {
        assertThat(payReturns(obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY,
                LocalDate.of(2025, 1, 31), Status.ACTIVE)).nextPaymentDate())
                .isEqualTo(LocalDate.of(2025, 2, 28));
        assertThat(payReturns(obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY,
                LocalDate.of(2024, 1, 31), Status.ACTIVE)).nextPaymentDate())
                .isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    void payRecordsPaymentWithObligationAmountAndCurrency() {
        Obligation o = obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY, LocalDate.of(2025, 3, 15), Status.ACTIVE);
        when(repository.findById(o.id())).thenReturn(Optional.of(o));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        when(repository.savePayment(payment.capture())).thenAnswer(inv -> inv.getArgument(0));

        PaidObligation result = service.pay(o.id());

        assertThat(payment.getValue().obligationId()).isEqualTo(o.id());
        assertThat(payment.getValue().amount()).isEqualTo(o.amount());
        assertThat(payment.getValue().currency()).isEqualTo(o.currency());
        assertThat(result.payment()).isEqualTo(payment.getValue());
    }

    @Test
    void payNonActiveThrows422() {
        Obligation expired = obligation(Category.BILL, null, LocalDate.now().minusDays(1), Status.EXPIRED);
        when(repository.findById(expired.id())).thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.pay(expired.id()))
                .isInstanceOf(InvalidObligationStateException.class);
        verify(repository, never()).savePayment(any());
    }

    @Test
    void cancelActiveSetsCancelled() {
        Obligation active = obligation(Category.SUBSCRIPTION, Recurrence.MONTHLY, LocalDate.now().plusDays(5), Status.ACTIVE);
        when(repository.findById(active.id())).thenReturn(Optional.of(active));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.cancel(active.id()).status()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void cancelNonActiveThrows422() {
        Obligation cancelled = obligation(Category.BILL, null, LocalDate.now(), Status.CANCELLED);
        when(repository.findById(cancelled.id())).thenReturn(Optional.of(cancelled));
        assertThatThrownBy(() -> service.cancel(cancelled.id()))
                .isInstanceOf(InvalidObligationStateException.class);
    }

    @Test
    void payMissingObligationThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.pay(id)).isInstanceOf(ObligationNotFoundException.class);
    }

    private Obligation payReturns(Obligation o) {
        when(repository.findById(o.id())).thenReturn(Optional.of(o));
        when(repository.savePayment(any())).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Obligation> saved = ArgumentCaptor.forClass(Obligation.class);
        when(repository.save(saved.capture())).thenAnswer(inv -> inv.getArgument(0));
        service.pay(o.id());
        return saved.getValue();
    }
}
