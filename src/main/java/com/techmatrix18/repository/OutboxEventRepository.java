package com.techmatrix18.repository;

import com.techmatrix18.model.OutboxEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

/**
 * Реактивный репозиторий для управления событиями Outbox.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.07.2026
 */
@Repository
public interface OutboxEventRepository extends R2dbcRepository<OutboxEvent, UUID> {

    /**
     * Выборка всех необработанных событий для Kafka-воркера.
     * Использует partial-индекс idx_outbox_unprocessed для максимальной скорости.
     */
    @Query("SELECT * FROM outbox_events WHERE processed = false ORDER BY created_at ASC")
    Flux<OutboxEvent> findAllUnprocessed();
}

