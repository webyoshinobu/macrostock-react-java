package io.github.yoshinobu_shibata.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

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
     * created_by_source / updated_by_source に入れる値(本来は"誰が"だが、
     * 今回は"どのAPIエンドポイントが"更新したかを表す文字列として使う)を返す実装。
     */
    static class RequestSourceAuditorAware implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            // TODO: RequestSourceInterceptor実装後、ThreadLocalから
            //       "POST /api/photos" のようなエンドポイント文字列を取得するよう差し替える
            // 現時点ではInterceptor未実装のため、仮の固定値を返している
            return Optional.of("SYSTEM");
        }
    }
}