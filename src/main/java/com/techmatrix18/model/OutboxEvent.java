package com.techmatrix18.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Сущность для паттерна Transactional Outbox.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.07.2026
 */
@Table(name = "outbox_events")
public class OutboxEvent implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("aggregate_type")
    private String aggregateType;

    @Column("aggregate_id")
    private String aggregateId;

    @Column("event_type")
    private String eventType;

    @Column("payload")
    private String payload; // Строка JSONB для хранения тела события

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("processed")
    private Boolean processed;

    @Column("processed_at")
    private LocalDateTime processedAt;

    @Transient
    private boolean isNewEntry = true; // Флаг для управления поведением INSERT в R2DBC

    public OutboxEvent() {
    }

    @Override
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    @Override
    @Transient
    public boolean isNew() { return isNewEntry; }
    public void setNewEntry(boolean isNewEntry) { this.isNewEntry = isNewEntry; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxEvent that)) return false;
        return Objects.equals(getId(), that.getId()) &&
            Objects.equals(getAggregateType(), that.getAggregateType()) &&
            Objects.equals(getAggregateId(), that.getAggregateId()) &&
            Objects.equals(getEventType(), that.getEventType()) &&
            Objects.equals(getPayload(), that.getPayload()) &&
            Objects.equals(getProcessed(), that.getProcessed());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, aggregateType, aggregateId, eventType, payload, processed);
    }

    @Override
    public String toString() {
        return "OutboxEvent {" +
            "id=" + id +
            ", aggregateType='" + aggregateType + '\'' +
            ", aggregateId='" + aggregateId + '\'' +
            ", eventType='" + eventType + '\'' +
            ", processed=" + processed +
            '}';
    }
}

