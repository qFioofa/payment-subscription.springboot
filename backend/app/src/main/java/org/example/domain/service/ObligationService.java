package org.example.domain.service;

import java.util.List;
import java.util.UUID;
import org.example.domain.model.Category;
import org.example.domain.model.CreatedObligation;
import org.example.domain.model.Obligation;
import org.example.domain.model.PaidObligation;
import org.example.domain.model.Status;
import org.example.domain.model.UpcomingObligations;

public interface ObligationService {

    CreatedObligation create(Obligation obligation);

    List<Obligation> findAll(Category category, Status status);

    UpcomingObligations findUpcoming(int days);

    PaidObligation pay(UUID id);

    Obligation cancel(UUID id);

    void delete(UUID id);
}
