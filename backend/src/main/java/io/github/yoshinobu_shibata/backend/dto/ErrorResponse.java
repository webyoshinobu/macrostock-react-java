package io.github.yoshinobu_shibata.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * API全体で統一して使うエラーレスポンスの形式。
 * VALIDATION_ERROR_HANDLING_POLICY.md で定めたレスポンス形式に対応する。
 *
 * 例:
 * {
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "入力内容に誤りがあります",
 *     "details": [ { "field": "email", "message": "..." } ]
 *   }
 * }
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final ErrorBody error;

    @Getter
    @AllArgsConstructor
    public static class ErrorBody {
        // エラーの種類を表す機械可読なコード(例: "VALIDATION_ERROR", "NOT_FOUND")
        private final String code;

        // 人間が読むためのエラーメッセージ
        private final String message;

        // バリデーションエラー時、どの項目が何の理由でエラーになったかの一覧(該当なしならnull可)
        private final List<FieldError> details;
    }

    @Getter
    @AllArgsConstructor
    public static class FieldError {
        // エラーが起きた入力項目名(例: "email")
        private final String field;
        // その項目に対するエラーメッセージ
        private final String message;
    }
}