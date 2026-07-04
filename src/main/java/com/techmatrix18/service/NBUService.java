package com.techmatrix18.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service class for handling NBU (National Bank of Ukraine) related operations.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 25.12.2025
 */
@Service
public class NBUService {

    private final WebClient webClient;

    public NBUService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange").build();
    }

    // TODO: Сейчас получает данные, но ничего с ними не делает (!) вместо поля rate у меня buy_price и sell_price (!)
    // Start by scheduled
    @Scheduled(cron = "0 0 * * * *") // every an hour
    //@Scheduled(cron = "0 * * * * *") // every a minute (for testing)
    //@Scheduled(cron = "*/30 * * * * *") // every 30 seconds (for testing)
    public void fetchCurrencyRates() {
        webClient.get()
            .uri("?json") // example for NBU API
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(System.out::println) // here can add saving into DB
            .subscribe();
    }
}

