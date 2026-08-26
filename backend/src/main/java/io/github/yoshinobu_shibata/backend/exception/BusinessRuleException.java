package io.github.yoshinobu_shibata.backend.exception;

/**
 * 業務ルール違反を表す例外(例: 出品者が自分の写真をカートに追加しようとした場合)。
 * GlobalExceptionHandler で 400 Bad Request として処理される。
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}