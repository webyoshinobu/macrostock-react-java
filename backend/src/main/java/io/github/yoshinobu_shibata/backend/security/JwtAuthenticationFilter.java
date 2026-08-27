package io.github.yoshinobu_shibata.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * リクエストごとにAuthorizationヘッダーのJWTを検証し、
 * 有効であればSecurityContextに認証情報をセットするフィルター。
 * 全リクエストで1回だけ実行される(OncePerRequestFilter)。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserId(token);
            String role = jwtTokenProvider.getRole(token);

            // Spring Securityが理解できる「認証済み」情報を作成
            // ROLE_ プレフィックスはSpring Securityの慣習(hasRole("SELLER")は内部的にROLE_SELLERを見る)
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // 以降の処理(コントローラー等)から「認証済みユーザー」として扱われるようになる
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // トークンがない/不正な場合はそのまま次へ渡す(未認証のまま処理が進み、
        // 保護されたエンドポイントであればSecurityConfigの設定により401/403になる)
        filterChain.doFilter(request, response);
    }

    // "Authorization: Bearer xxxxx" からトークン部分だけを取り出す
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}