package org.example.di;

import org.example.datasource.mapper.ObligationEntityMapper;
import org.example.datasource.mapper.PaymentEntityMapper;
import org.example.datasource.repository.ObligationJpaRepository;
import org.example.datasource.repository.ObligationRepository;
import org.example.datasource.repository.ObligationRepositoryImpl;
import org.example.datasource.repository.PaymentJpaRepository;
import org.example.domain.service.ObligationService;
import org.example.domain.service.ObligationServiceImpl;
import org.example.web.mapper.ObligationWebMapper;
import org.example.web.mapper.PaymentWebMapper;
import org.example.web.sse.ObligationEvents;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ObligationEntityMapper obligationEntityMapper() {
        return new ObligationEntityMapper();
    }

    @Bean
    public PaymentEntityMapper paymentEntityMapper() {
        return new PaymentEntityMapper();
    }

    @Bean
    public ObligationWebMapper obligationWebMapper() {
        return new ObligationWebMapper();
    }

    @Bean
    public PaymentWebMapper paymentWebMapper() {
        return new PaymentWebMapper();
    }

    @Bean
    public ObligationEvents obligationEvents() {
        return new ObligationEvents();
    }

    @Bean
    public ObligationRepository
    obligationRepository(ObligationJpaRepository obligations,
                         PaymentJpaRepository payments,
                         ObligationEntityMapper obligationMapper,
                         PaymentEntityMapper paymentMapper) {
        return new ObligationRepositoryImpl(obligations, payments,
                                            obligationMapper, paymentMapper);
    }

    @Bean
    public ObligationService
    obligationService(ObligationRepository repository) {
        return new ObligationServiceImpl(repository);
    }
}
