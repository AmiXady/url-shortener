package kz.amixady.urlshortener.link.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Превращает исключения в ответы формата Problem Details (RFC 7807).
 * <p>
 * Наследуемся от ResponseEntityExceptionHandler: он уже обрабатывает стандартные ошибки
 * Spring MVC (кривой JSON, неверный метод, неизвестный путь и т. д.) в том же формате.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_ERROR_MESSAGE = "Внутренняя ошибка, попробуйте позже";

    // ======================== 400: невалидный запрос ========================

    /**
     * @Valid на теле запроса: добавляем ошибки по полям.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problem = ex.getBody();
        problem.setTitle("Invalid request");
        problem.setDetail("Запрос содержит ошибки");
        problem.setProperty("errors", fieldErrors(ex));
        return ResponseEntity.status(status).headers(headers).body(problem);
    }

    // ======================== 403: нет доступа ========================

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException e) {
        return problem(HttpStatus.FORBIDDEN, "Access denied", e.getMessage());
    }

    // ======================== 404: не найдено ========================

    @ExceptionHandler(LinkNotFoundException.class)
    public ProblemDetail handleLinkNotFound(LinkNotFoundException e) {
        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "Link not found", e.getMessage());
        problem.setProperty("code", e.getCode());
        return problem;
    }

    // ======================== 409: конфликт кода ========================

    @ExceptionHandler(CodeAlreadyTakenException.class)
    public ProblemDetail handleCodeTaken(CodeAlreadyTakenException e) {
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "Code already taken", e.getMessage());
        problem.setProperty("code", e.getCode());
        return problem;
    }

    @ExceptionHandler(CodeReservedException.class)
    public ProblemDetail handleCodeReserved(CodeReservedException e) {
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "Code reserved", e.getMessage());
        problem.setProperty("code", e.getCode());
        return problem;
    }

    // ======================== 503: временная проблема ========================

    /**
     * Не баг, а невезение с кодами: повтор почти наверняка сработает.
     */
    @ExceptionHandler(CodeGenerationException.class)
    public ProblemDetail handleCodeGeneration(CodeGenerationException e) {
        log.error("Code generation failed", e);
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Code generation failed",
                "Не удалось создать ссылку, попробуйте ещё раз");
    }

    // ======================== 500: всё остальное ========================

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDatabaseError(DataAccessException e) {
        log.error("Database error", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", INTERNAL_ERROR_MESSAGE);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", INTERNAL_ERROR_MESSAGE);
    }

    //stats serivce
    @ExceptionHandler(StatsUnavailableException.class)
    public ProblemDetail handleStatsUnavailable(StatsUnavailableException e) {
        log.warn("Stats service unavailable: {}", e.getMessage());
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Stats unavailable",
                "Статистика временно недоступна, попробуйте позже");
    }

    // ======================== Helpers ========================

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }

    /**
     * Поле → сообщения об ошибках. Несколько ошибок одного поля склеиваются через "; ".
     */
    private static Map<String, String> fieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> String.valueOf(error.getDefaultMessage()),
                        (first, second) -> first + "; " + second,
                        TreeMap::new));
    }
}