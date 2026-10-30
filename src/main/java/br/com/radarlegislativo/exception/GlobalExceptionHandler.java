package br.com.radarlegislativo.exception;

import br.com.radarlegislativo.log.AppLog;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        AppLog.logger(log).system().does("validar request").failed(errors.toString()).warn();
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "dados inválidos", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        AppLog.logger(log).system().does("validar parâmetros").failed(errors.toString()).warn();
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "dados inválidos", errors));
    }

    @ExceptionHandler({TypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleUnreadable(Exception ex) {
        AppLog.logger(log).system().does("ler request").failed(ex.getClass().getSimpleName()).warn();
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "dados inválidos", List.of()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        AppLog.logger(log).system().does("buscar recurso").failed("não encontrado").warn();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "recurso não encontrado", List.of()));
    }

    @ExceptionHandler(FonteIndisponivelException.class)
    public ResponseEntity<ApiError> handleFonteIndisponivel(FonteIndisponivelException ex) {
        AppLog.logger(log).system().does("consultar API pública").on("fonte=" + ex.fonte()).failed(ex.getMessage()).warn();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiError.of(HttpStatus.BAD_GATEWAY, "API pública indisponível", List.of("fonte: " + ex.fonte())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        AppLog.logger(log).system().does("validar regra de domínio").failed(ex.getMessage()).warn();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        // Exceções do próprio Spring MVC (404 de rota, 405, parâmetro faltando...) já sabem o status certo.
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            AppLog.logger(log).system().does("processar request").failed(ex.getClass().getSimpleName()).warn();
            return ResponseEntity.status(status)
                    .body(ApiError.of(status, status.getReasonPhrase(), List.of()));
        }
        AppLog.logger(log).system().does("processar request").failed(ex.getClass().getSimpleName()).error(ex);
        return ResponseEntity.internalServerError()
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "erro interno", List.of()));
    }
}
