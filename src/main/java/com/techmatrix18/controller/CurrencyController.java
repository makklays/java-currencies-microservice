package com.techmatrix18.controller;

import com.techmatrix18.model.CurrencyCqrsRead;
import com.techmatrix18.service.CurrencyCommandService;
import com.techmatrix18.service.CurrencyQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller class for managing Currency entities using CQRS pattern.
 *
 * OpenAPI documentation is available at:
 * http://localhost:8081/webjars/swagger-ui/index.html
 * http://localhost:8081/v3/api-docs
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.07.2026
 */
@RestController
@RequestMapping(path = "/api/v1/currencies", produces = "application/json")
@CrossOrigin(origins = "*")
@Tag(name = "Валюты (CQRS)", description = "Управление коммерческими курсами валют в рамках распределенных транзакций Сага")
public class CurrencyController {

    private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);
    private final CurrencyCommandService currencyCommandService;
    private final CurrencyQueryService currencyQueryService;

    public CurrencyController(CurrencyCommandService currencyCommandService,
                              CurrencyQueryService currencyQueryService) {
        this.currencyCommandService = currencyCommandService;
        this.currencyQueryService = currencyQueryService;
    }

    @Operation(summary = "Проверка связи", description = "Тестовый эндпоинт для проверки реактивного окружения")
    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello, reactive world!");
    }

    @Operation(summary = "Получить список недавних валют", description = "Возвращает до 12 последних обновленных валют из read-оптимизированной таблицы")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = CurrencyCqrsRead.class))))
    @GetMapping(params = "recent")
    public Flux<CurrencyCqrsRead> recentCurrency() {
        return currencyQueryService.findAll().take(12);
    }

    @Operation(summary = "Получить валюту по ID", description = "Поиск валюты в CQRS read-модели по числовому идентификатору")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Валюта найдена",
            content = @Content(schema = @Schema(implementation = CurrencyCqrsRead.class))),
        @ApiResponse(responseCode = "404", description = "Валюта с таким ID не найдена", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<CurrencyCqrsRead> currencyById(@Parameter(description = "Внутренний ID записи", example = "1") @PathVariable("id") Long id) {
        return currencyQueryService.findById(id);
    }

    @Operation(summary = "Получить курс по коду (Сага бэкап)", description = "Используется API-шлюзом на Шаге 1 Саги для сохранения старого курса перед обновлением")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Курс успешно получен",
            content = @Content(schema = @Schema(implementation = CurrencyCqrsRead.class))),
        @ApiResponse(responseCode = "404", description = "Код валюты не найден в системе", content = @Content)
    })
    @GetMapping("/code/{code}")
    public Mono<ResponseEntity<CurrencyCqrsRead>> getCurrencyByCode(
        @Parameter(name = "X-Correlation-ID", in = ParameterIn.HEADER, description = "Сквозной ID распределенной транзакции", example = "550e8400-e29b-41d4-a716-446655440000")
        @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
        @Parameter(description = "ISO-код валюты", example = "USD") @PathVariable("code") String code) {

        log.info("[Correlation-ID: {}] CQRS-Read: Запрос текущего курса для валюты: {}", correlationId != null ? correlationId : "NOT_FOUND", code);

        return currencyQueryService.findByCc(code.toUpperCase())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Обновить коммерческий курс (Сага / Идемпотентность)",
        description = "Ветка записи команды. Вызывается оркестратором шлюза. Защищена идемпотентностью по заголовку X-Correlation-ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Курс успешно обновлен (или запрос проигнорирован как дубликат)"),
        @ApiResponse(responseCode = "400", description = "Неверная структура JSON, отсутствуют обязательные поля 'buy' или 'sell'"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка при выполнении операции записи")
    })
    @PutMapping("/code/{code}")
    public Mono<ResponseEntity<Void>> updateCurrencyRate(
        @Parameter(name = "X-Correlation-ID", in = ParameterIn.HEADER, required = true, description = "Уникальный ID Саги, выступающий ключом идемпотентности", example = "550e8400-e29b-41d4-a716-446655440000")
        @RequestHeader(value = "X-Correlation-ID") String correlationId,
        @Parameter(description = "ISO-код валюты для обновления", example = "EUR") @PathVariable("code") String code,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Новые цены покупки и продажи коммерческого банка",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(value = "{\"buy\": 41.20, \"sell\": 41.85, \"updatedAt\": \"2026-07-05T01:20:00\"}")
            )
        )
        @RequestBody Map<String, Object> rateData) {

        log.info("[Correlation-ID: {}] CQRS-Command: Получена команда на обновление курса {}. Данные: {}", correlationId, code, rateData);

        Double buyPrice = rateData.get("buy") != null ? Double.valueOf(rateData.get("buy").toString()) : null;
        Double sellPrice = rateData.get("sell") != null ? Double.valueOf(rateData.get("sell").toString()) : null;
        String updatedAtStr = rateData.get("updatedAt") != null ? rateData.get("updatedAt").toString() : null;

        if (buyPrice == null || sellPrice == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return currencyCommandService.updateCurrencyRate(correlationId, code.toUpperCase(), buyPrice, sellPrice, updatedAtStr)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .onErrorResume(e -> {
                log.error("[Correlation-ID: {}] Ошибка выполнения команды обновления курса: {}", correlationId, e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }
}

