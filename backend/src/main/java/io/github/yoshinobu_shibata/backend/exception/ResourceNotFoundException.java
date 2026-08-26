package io.github.yoshinobu_shibata.backend.exception;

/**
 * 指定されたリソース(写真、注文など)が見つからない場合に投げる例外。
 * GlobalExceptionHandler で 404 Not Found として処理される。
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}