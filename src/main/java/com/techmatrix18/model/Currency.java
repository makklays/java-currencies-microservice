package com.techmatrix18.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Simple JavaBean domain that represents a Currency
 * from REST API NBU - Nation Bank Ukraine - URL: https://bank.gov.ua/en/open-data/api-dev
 * Rate for the Current Date - URL: https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json
 * Rate for the Specific Date, date is specified in format: yyyymmdd, where yyyy is year, mm is month, dd is day:
 * https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date=20200302&json
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 10.06.2026
 */
@Table(name = "currencies")
public class Currency {

    @Id
    @Column("id")
    private Long id;

    @Column("cc")
    private String cc;

    @Column("r030")
    private Integer r030;

    @Column("title")
    private String title;

    @Column("rate")
    private Float rate;

    @Column("exchangedate")
    private String exchangedate;

    @Column("created_at")
    private LocalDateTime createdAt;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCc() { return cc; }
    public void setCc(String cc) { this.cc = cc; }

    public Integer getR030() { return r030; }
    public void setR030(Integer r030) { this.r030 = r030; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Float getRate() { return rate; }
    public void setRate(Float rate) { this.rate = rate; }

    public String getExchangedate() { return exchangedate; }
    public void setExchangedate(String exchangedate) { this.exchangedate = exchangedate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency currency)) return false;
        return getId().equals(currency.getId()) && getCc().equals(currency.getCc()) && getR030().equals(currency.getR030()) &&
            getTitle().equals(currency.getTitle()) && getRate().equals(currency.getRate()) &&
            getExchangedate().equals(currency.getExchangedate()) && getCreatedAt().equals(currency.getCreatedAt());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (cc != null ? cc.hashCode() : 0);
        result = 31 * result + (r030 != null ? r030.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (rate != null ? rate.hashCode() : 0);
        result = 31 * result + (exchangedate != null ? exchangedate.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "Currency {" +
            "id=" + id +
            ", cc='" + cc + '\'' +
            ", r030=" + r030 +
            ", title=" + title +
            ", rate=" + rate +
            ", exchangedate='" + exchangedate + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}

