package io.github.yoshinobu_shibata.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
/**
 * JPA監査機能 有効化コンフィグクラス
 *
 * <p>
 *  @EnableJpaAuditingはアノテーションだけで機能が有効化される仕組みのため
 *  、フィールドやメソッドは不要
 * </p>
 *
 * @author  Yoshinobu Shibata
 * @version 1.0
 * @since   2026-08-25
 */
public class JpaAuditingConfig {
}
