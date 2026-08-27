package io.github.yoshinobu_shibata.backend.config;

import io.github.yoshinobu_shibata.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Securityの認可ルール設定。
 * docs/openapi.yaml のエンドポイント一覧(🔒/👤SELLERマーク)に対応させる。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // パスワードのハッシュ化に使うエンコーダー(Auth機能実装時に使用)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF対策はセッションCookie前提の仕組みのため、JWT(トークン方式)では不要
                .csrf(csrf -> csrf.disable())
                // セッションを使わず、リクエストごとにJWTで認証する(ステートレス)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 認証不要(誰でもアクセス可) — openapi.yamlで🔒が付いていないもの
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/webhooks/stripe",
                                "/api/downloads/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/photos", "/api/photos/**").permitAll()

                        // 出品者専用(👤SELLERマーク) — ROLE_SELLERを持つユーザーのみ
                        .requestMatchers(HttpMethod.POST, "/api/photos").hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/photos/**").hasRole("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/photos/**").hasRole("SELLER")
                        .requestMatchers("/api/seller/**").hasRole("SELLER")

                        // それ以外(🔒マークのみ)は、ログイン済みであれば誰でもOK
                        .anyRequest().authenticated()
                )
                // 自作のJWTフィルターを、Spring標準のログイン処理フィルターより前に実行する
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}