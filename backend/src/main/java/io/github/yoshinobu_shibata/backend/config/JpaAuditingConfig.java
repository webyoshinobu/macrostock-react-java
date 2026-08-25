package io.github.yoshinobu_shibata.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")

/**
 * JPA監査機能 有効化コンフィグクラス
 *
 * @author  Yoshinobu Shibata
 * @version 1.0
 * @since   2026-08-25
 */
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new RequestSourceAuditorAware();
    }

    static class RequestSourceAuditorAware implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            // TODO: RequestSourceInterceptor実装後、ThreadLocalから
            //       "POST /api/photos" のようなエンドポイント文字列を取得するよう差し替える
            return Optional.of("SYSTEM");
        }
    }
}