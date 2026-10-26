package kz.amixady.urlshortener.link.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import kz.amixady.urlshortener.link.client.ClickStats;
import kz.amixady.urlshortener.link.client.StatsClient;
import kz.amixady.urlshortener.link.dto.request.BatchCreateLinkRequest;
import kz.amixady.urlshortener.link.dto.request.CreateLinkRequest;
import kz.amixady.urlshortener.link.dto.response.BatchCreateLinkResponse;
import kz.amixady.urlshortener.link.dto.response.BatchCreateLinkResponse.ItemResult;
import kz.amixady.urlshortener.link.dto.response.LinkResponse;
import kz.amixady.urlshortener.link.dto.response.LinkStatsResponse;
import kz.amixady.urlshortener.link.entity.Link;
import kz.amixady.urlshortener.link.exception.CodeAlreadyTakenException;
import kz.amixady.urlshortener.link.exception.CodeGenerationException;
import kz.amixady.urlshortener.link.exception.CodeReservedException;
import kz.amixady.urlshortener.link.exception.LinkNotFoundException;
import kz.amixady.urlshortener.link.mapper.LinkMapper;
import kz.amixady.urlshortener.link.repository.LinkRepository;
import kz.amixady.urlshortener.link.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private static final String CODE_UNIQUE_CONSTRAINT = "uk_links_code";
    private static final Set<String> RESERVED_CODES = Set.of("api", "manage", "health", "actuator");

    private final LinkRepository repository;
    private final LinkMapper mapper;
    private final CodeGenerator codeGenerator;
    private final TokenService tokenService;
    private final StatsClient statsClient;
    private final Validator validator;
    private final Clock clock;

    // ======================== Public API ========================

    /**
     * UC-1. Ожидает уже провалидированный запрос.
     * Без @Transactional: каждая попытка сохранения — отдельная транзакция.
     */
    public LinkResponse create(CreateLinkRequest request) {
        String token = tokenService.generate();
        String tokenHash = tokenService.hash(token);

        Link saved = request.hasCustomCode()
                ? saveWithCustomCode(request, tokenHash)
                : saveWithGeneratedCode(request, tokenHash);

        return mapper.toResponse(saved, token);
    }

    /**
     * UC-2. Без @Transactional: элементы сохраняются независимо,
     * ошибка одного не откатывает остальные.
     */
    public BatchCreateLinkResponse createBatch(BatchCreateLinkRequest request) {
        List<CreateLinkRequest> items = request.items();
        List<ItemResult> results = new ArrayList<>(items.size());

        for (int index = 0; index < items.size(); index++) {
            results.add(createItem(index, items.get(index)));
        }
        return new BatchCreateLinkResponse(results);
    }

    /**
     * UC-6. Мягкое удаление. Повторное удаление ничего не меняет.
     */
    @Transactional
    public void delete(String code, String token) {
        Link link = findByCode(code);
        tokenService.verify(token, link.getTokenHash());
        link.markDeleted(clock.instant());
    }


    public LinkStatsResponse getStats(String code, String token) {
        Link link = findByCode(code);
        tokenService.verify(token, link.getTokenHash());

        ClickStats clickStats = statsClient.getStats(link.getId());

        return new LinkStatsResponse(
                link.getCode(),
                clickStats.clicks(),
                link.status(clock.instant()),
                clickStats.updatedAt()
        );
    }

    // ======================== Batch ========================

    private ItemResult createItem(int index, CreateLinkRequest item) {
        String validationError = validateItem(item);
        if (validationError != null) {
            return ItemResult.failed(index, validationError);
        }

        try {
            return ItemResult.created(index, create(item));
        } catch (CodeAlreadyTakenException | CodeReservedException e) {
            return ItemResult.failed(index, e.getMessage());
        } catch (CodeGenerationException e) {
            log.error("Code generation failed for batch item {}", index, e);
            return ItemResult.failed(index, "Не удалось создать ссылку, попробуйте ещё раз");
        }
        // Ошибки БД не ловим: если база недоступна, она недоступна для всех элементов
    }

    /**
     * @return null, если элемент валиден, иначе текст ошибки
     */
    private String validateItem(CreateLinkRequest item) {
        if (item == null) {
            return "Пустой элемент";
        }

        Set<ConstraintViolation<CreateLinkRequest>> violations = validator.validate(item);
        if (violations.isEmpty()) {
            return null;
        }

        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("; "));
    }

    // ======================== Saving ========================

    private Link saveWithGeneratedCode(CreateLinkRequest request, String tokenHash) {
        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate();
            try {
                return repository.saveAndFlush(buildLink(request, code, tokenHash));
            } catch (DataIntegrityViolationException e) {
                rethrowIfNotCodeConflict(e);
                log.warn("Generated code collision '{}', attempt {}/{}", code, attempt, MAX_GENERATION_ATTEMPTS);
            }
        }
        throw new CodeGenerationException(request.originalUrl());
    }

    private Link saveWithCustomCode(CreateLinkRequest request, String tokenHash) {
        Link link = buildLink(request, request.customCode(), tokenHash);
        String code = link.getCode();   // уже в нижнем регистре

        if (RESERVED_CODES.contains(code)) {
            throw new CodeReservedException(code);
        }

        try {
            return repository.saveAndFlush(link);
        } catch (DataIntegrityViolationException e) {
            rethrowIfNotCodeConflict(e);
            throw new CodeAlreadyTakenException(code);
        }
    }

    private Link buildLink(CreateLinkRequest request, String code, String tokenHash) {
        return request.hasExpiration()
                ? Link.createExpiring(code, request.originalUrl(), request.expiresAt(), tokenHash)
                : Link.createPermanent(code, request.originalUrl(), tokenHash);
    }


    // ======================== Helpers ========================

    private Link findByCode(String code) {
        return repository.findByCode(Link.normalizeCode(code))
                .orElseThrow(() -> new LinkNotFoundException(code));
    }

    /**
     * Пробрасывает исключение дальше, если это не конфликт уникальности кода.
     */
    private void rethrowIfNotCodeConflict(DataIntegrityViolationException e) {
        if (!isCodeConflict(e)) {
            throw e;
        }
    }

    private boolean isCodeConflict(DataIntegrityViolationException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException cve) {
                return CODE_UNIQUE_CONSTRAINT.equalsIgnoreCase(cve.getConstraintName());
            }
        }
        return false;
    }
}