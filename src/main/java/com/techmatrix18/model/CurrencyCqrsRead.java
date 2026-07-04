package com.techmatrix18.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * This class is a placeholder for the CurrencyCqrsRead model, which will be used for read operations in a CQRS (Command Query Responsibility Segregation) architecture.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 10.06.2026
 */
@Table(name = "currencies_cqrs_read")
public class CurrencyCqrsRead {

    @Id
    @Column("id")
    private Long id;

    @Column("cc")
    private String cc;

    @Column("r030")
    private Integer r030;

    @Column("title")
    private String title;

    @Column("buy_price")
    private Double buyPrice;

    @Column("sell_price")
    private Double sellPrice;

    @Column("exchangedate")
    private String exchangedate;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCc() { return cc; }
    public void setCc(String cc) { this.cc = cc; }

    public Integer getR030() { return r030; }
    public void setR030(Integer r030) { this.r030 = r030; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(Double buyPrice) { this.buyPrice = buyPrice; }

    public Double getSellPrice() { return sellPrice; }
    public void setSellPrice(Double sellPrice) { this.sellPrice = sellPrice; }

    public String getExchangedate() { return exchangedate; }
    public void setExchangedate(String exchangedate) { this.exchangedate = exchangedate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrencyCqrsRead that)) return false;
        return getId().equals(that.getId()) &&
            getCc().equals(that.getCc()) &&
            getR030().equals(that.getR030()) &&
            getTitle().equals(that.getTitle()) &&
            getBuyPrice().equals(that.getBuyPrice()) &&
            getSellPrice().equals(that.getSellPrice()) &&
            getExchangedate().equals(that.getExchangedate()) &&
            getUpdatedAt().equals(that.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (cc != null ? cc.hashCode() : 0);
        result = 31 * result + (r030 != null ? r030.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (buyPrice != null ? buyPrice.hashCode() : 0);
        result = 31 * result + (sellPrice != null ? sellPrice.hashCode() : 0);
        result = 31 * result + (exchangedate != null ? exchangedate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CurrencyCqrsRead {" +
            "id=" + id +
            ", cc='" + cc + '\'' +
            ", r030=" + r030 +
            ", title=" + title +
            ", buyPrice=" + buyPrice +
            ", sellPrice=" + sellPrice +
            ", exchangedate='" + exchangedate + '\'' +
            ", updatedAt=" + updatedAt +
            '}';
    }
}

