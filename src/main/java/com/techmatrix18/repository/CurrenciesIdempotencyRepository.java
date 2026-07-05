package com.techmatrix18.repository;

import com.techmatrix18.model.CurrenciesIdempotency;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Реактивный репозиторий для проверки ключей идемпотентности Саги.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.07.2026
 */
@Repository
public interface CurrenciesIdempotencyRepository extends R2dbcRepository<CurrenciesIdempotency, String> {
    // Базовых методов R2dbcRepository (findById, save) полностью достаточно для логики Саги
}

