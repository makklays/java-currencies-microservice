package com.techmatrix18.repository;

import com.techmatrix18.model.CurrencyCqrsRead;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for performing read-only CRUD operations on CurrencyCqrsRead entities.
 * Used for the query side of the CQRS pattern.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 11.06.2026
 */
@Repository
public interface CurrencyCqrsReadRepository extends ReactiveCrudRepository<CurrencyCqrsRead, Long> {

    /**
     * Finds a currency read-model by its currency code (e.g., USD, EUR).
     *
     * @param cc the currency code
     * @return a Mono containing the matching CurrencyCqrsRead, or empty if none found
     */
    Mono<CurrencyCqrsRead> findByCc(String cc);

    /**
     * Finds all currency read-models for a specific exchange date.
     *
     * @param exchangedate the exchange date string
     * @return a Flux of matching CurrencyCqrsRead entities
     */
    Flux<CurrencyCqrsRead> findByExchangedate(String exchangedate);
}

