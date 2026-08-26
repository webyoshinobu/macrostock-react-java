package io.github.yoshinobu_shibata.backend.exception;

import io.github.yoshinobu_shibata.backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * アプリ全体の例外を一括してキャッチし、統一されたErrorResponse形式に変換するクラス。
 * VALIDATION_ERROR_HANDLING_POLICY.md の「バックエンド側」方針に対応する。
 *
 * @RestControllerAdvice が付いたクラスは、すべての@RestControllerで発生した
 * 例外をここに集約して処理できる(コントローラーごとにtry-catchを書く必要がなくなる)。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid付きのDTOでバリデーションエラーが起きた場合(400 Bad Request)
    // 例: RegisterRequestのemailが不正な形式だった場合など
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        // どの項目が、どんな理由でエラーになったかを一覧化する
        List<ErrorResponse.FieldError> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse body = new ErrorResponse(
                new ErrorResponse.ErrorBody("VALIDATION_ERROR", "入力内容に誤りがあります", details)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 業務ルール違反(自己購入禁止など)の場合(400 Bad Request)
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(BusinessRuleException ex) {
        ErrorResponse body = new ErrorResponse(
                new ErrorResponse.ErrorBody("BUSINESS_RULE_VIOLATION", ex.getMessage(), null)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // リソースが見つからない場合(404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                new ErrorResponse.ErrorBody("NOT_FOUND", ex.getMessage(), null)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 上記のいずれにも当てはまらない、想定外のエラー(500 Internal Server Error)
    // ここでキャッチしておくことで、Spring Bootのデフォルトのエラーページ(Whitelabel Error Page)
    // ではなく、統一形式のJSONレスポンスを返せるようになる
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
        ErrorResponse body = new ErrorResponse(
                new ErrorResponse.ErrorBody("INTERNAL_SERVER_ERROR", "予期しないエラーが発生しました", null)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}