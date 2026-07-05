package com.techmatrix18.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность для контроля идемпотентности входящих команд Саги.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.07.2026
 */
@Table(name = "currencies_idempotency")
public class CurrenciesIdempotency implements Persistable<String> {

    @Id
    @Column("idempotency_key")
    private String idempotencyKey;

    @Column("response_body")
    private String responseBody;

    @Column("status")
    private String status; // 'PROCESSING' или 'COMPLETED'

    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean isNewEntry = true; // Флаг для управления поведением INSERT в R2DBC

    public CurrenciesIdempotency() {
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    @Transient
    public String getId() { return idempotencyKey; }

    @Override
    @Transient
    public boolean isNew() { return isNewEntry; }
    public void setNewEntry(boolean isNewEntry) { this.isNewEntry = isNewEntry; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrenciesIdempotency that)) return false;
        return Objects.equals(getIdempotencyKey(), that.getIdempotencyKey()) &&
            Objects.equals(getStatus(), that.getStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(idempotencyKey, status);
    }

    @Override
    public String toString() {
        return "CurrenciesIdempotency {" +
            "idempotencyKey='" + idempotencyKey + '\'' +
            ", status='" + status + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}

