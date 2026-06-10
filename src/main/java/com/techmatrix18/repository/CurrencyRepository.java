package com.techmatrix18.repository;

import com.techmatrix18.model.Currency;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for performing CRUD operations on Currency entities.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 10.06.2026
 */
@Repository
public interface CurrencyRepository extends ReactiveCrudRepository<Currency, Long> {

    Mono<Currency> findByCc(String code); // code = EUR, USD

    Flux<Currency> findByExchangedate(String date);
}

