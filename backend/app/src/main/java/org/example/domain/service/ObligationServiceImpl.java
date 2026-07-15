package org.example.domain.service;

import java.util.List;
import java.util.UUID;
import org.example.datasource.repository.ObligationRepository;
import org.example.domain.model.Category;
import org.example.domain.model.CreatedObligation;
import org.example.domain.model.Obligation;
import org.example.domain.model.PaidObligation;
import org.example.domain.model.Status;
import org.example.domain.model.UpcomingObligations;

public class ObligationServiceImpl implements ObligationService {

    private final ObligationRepository repository;

    public ObligationServiceImpl(ObligationRepository repository) {
        this.repository = repository;
    }

    @Override
    public CreatedObligation create(Obligation obligation) {
        throw new UnsupportedOperationException("create is not implemented yet");
    }

    @Override
    public List<Obligation> findAll(Category category, Status status) {
        throw new UnsupportedOperationException("findAll is not implemented yet");
    }

    @Override
    public UpcomingObligations findUpcoming(int days) {
        throw new UnsupportedOperationException("findUpcoming is not implemented yet");
    }

    @Override
    public PaidObligation pay(UUID id) {
        throw new UnsupportedOperationException("pay is not implemented yet");
    }

    @Override
    public Obligation cancel(UUID id) {
        throw new UnsupportedOperationException("cancel is not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("delete is not implemented yet");
    }
}
