package io.github.yoshinobu_shibata.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

import io.github.yoshinobu_shibata.backend.interceptor.RequestSourceInterceptor;

/**
 * Spring Data JPAのAuditing(監査)機能を有効化する設定クラス。
 * これにより BaseEntity の @CreatedDate/@LastModifiedDate/@CreatedBy/@LastModifiedBy が
 * 保存・更新のたびに自動で値をセットされるようになる。
 */
@Configuration
// アプリ全体でAuditing機能を有効化。auditorAwareRefで指定した名前のBeanから
// "誰が(何が)更新したか"の情報を取得するよう紐付ける
@EnableJpaAuditing(auditorAwareRef = "auditorAware")

public class JpaAuditingConfig {

    // @CreatedBy/@LastModifiedByに入れる値を供給するBeanを登録
    @Bean
    public AuditorAware<String> auditorAware() {
        return new RequestSourceAuditorAware();
    }

    /**
     * created_by_source / updated_by_source に、
     * "どのAPIエンドポイントが更新したか"(例: "POST /api/photos")をセットする実装。
     * RequestSourceInterceptor がThreadLocalに保持した値をここで取り出す。
     */
    static class RequestSourceAuditorAware implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            // ThreadLocalに値がなければ(バッチ処理など、HTTPリクエスト経由でない保存の場合)
            // "SYSTEM"を代わりにセットする
            return Optional.ofNullable(RequestSourceInterceptor.getCurrentSource())
                    .or(() -> Optional.of("SYSTEM"));
        }
    }
}