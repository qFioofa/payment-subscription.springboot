package org.example.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.example.domain.model.Category;
import org.example.domain.model.PaidObligation;
import org.example.domain.model.Status;
import org.example.domain.service.ObligationService;
import org.example.web.mapper.ObligationWebMapper;
import org.example.web.mapper.PaymentWebMapper;
import org.example.web.model.CreateObligationResponse;
import org.example.web.model.ObligationRequest;
import org.example.web.model.ObligationResponse;
import org.example.web.model.PayResponse;
import org.example.web.model.UpcomingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/obligations")
public class ObligationController {

    private final ObligationService service;
    private final ObligationWebMapper obligationMapper;
    private final PaymentWebMapper paymentMapper;

    public ObligationController(
            ObligationService service,
            ObligationWebMapper obligationMapper,
            PaymentWebMapper paymentMapper
    ) {
        this.service = service;
        this.obligationMapper = obligationMapper;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateObligationResponse create(@Valid @RequestBody ObligationRequest request) {
        return obligationMapper.toResponse(service.create(obligationMapper.toDomain(request)));
    }

    @GetMapping
    public List<ObligationResponse> findAll(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Status status
    ) {
        return service.findAll(category, status).stream().map(obligationMapper::toResponse).toList();
    }

    @GetMapping("/upcoming")
    public UpcomingResponse findUpcoming(@RequestParam(defaultValue = "7") int days) {
        return obligationMapper.toResponse(service.findUpcoming(days));
    }

    @PostMapping("/{id}/pay")
    public PayResponse pay(@PathVariable UUID id) {
        PaidObligation paid = service.pay(id);
        return new PayResponse(
                obligationMapper.toResponse(paid.obligation()),
                paymentMapper.toResponse(paid.payment())
        );
    }

    @PatchMapping("/{id}/cancel")
    public ObligationResponse cancel(@PathVariable UUID id) {
        return obligationMapper.toResponse(service.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
