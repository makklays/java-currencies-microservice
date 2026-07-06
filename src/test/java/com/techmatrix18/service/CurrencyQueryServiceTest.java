package com.techmatrix18.service;

import com.techmatrix18.model.CurrencyCqrsRead;
import com.techmatrix18.repository.CurrencyCqrsReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

/**
 * Unit tests for the CurrencyQueryService class.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.07.2026
 */
@ExtendWith(MockitoExtension.class)
class CurrencyQueryServiceTest {

    @Mock
    private CurrencyCqrsReadRepository currencyCqrsReadRepo;

    @InjectMocks
    private CurrencyQueryService currencyQueryService;

    private CurrencyCqrsRead sampleCurrency;

    @BeforeEach
    void setUp() {
        sampleCurrency = new CurrencyCqrsRead();
        sampleCurrency.setId(1L);
        sampleCurrency.setCc("USD");
        sampleCurrency.setExchangedate("2026-07-06");
    }

    @Test
    void findAll_ShouldReturnFluxOfCurrencies() {
        // Given
        when(currencyCqrsReadRepo.findAll()).thenReturn(Flux.just(sampleCurrency));

        // When
        Flux<CurrencyCqrsRead> result = currencyQueryService.findAll();

        // Then
        StepVerifier.create(result)
            .expectNext(sampleCurrency)
            .verifyComplete();
    }

    @Test
    void findByExchangedate_ShouldReturnFluxOfCurrencies() {
        // Given
        String date = "2026-07-06";
        when(currencyCqrsReadRepo.findByExchangedate(date)).thenReturn(Flux.just(sampleCurrency));

        // When
        Flux<CurrencyCqrsRead> result = currencyQueryService.findByExchangedate(date);

        // Then
        StepVerifier.create(result)
            .expectNext(sampleCurrency)
            .verifyComplete();
    }

    @Test
    void findById_ShouldReturnMonoOfCurrency() {
        // Given
        Long id = 1L;
        when(currencyCqrsReadRepo.findById(id)).thenReturn(Mono.just(sampleCurrency));

        // When
        Mono<CurrencyCqrsRead> result = currencyQueryService.findById(id);

        // Then
        StepVerifier.create(result)
            .expectNext(sampleCurrency)
            .verifyComplete();
    }

    @Test
    void findByCc_ShouldReturnMonoOfCurrency() {
        // Given
        String code = "USD";
        when(currencyCqrsReadRepo.findByCc(code)).thenReturn(Mono.just(sampleCurrency));

        // When
        Mono<CurrencyCqrsRead> result = currencyQueryService.findByCc(code);

        // Then
        StepVerifier.create(result)
            .expectNext(sampleCurrency)
            .verifyComplete();
    }
}

