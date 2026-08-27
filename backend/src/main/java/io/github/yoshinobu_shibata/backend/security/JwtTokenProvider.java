package io.github.yoshinobu_shibata.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT(JSON Web Token)の発行・検証を担当するクラス。
 * アクセストークンには userId と role をペイロードとして含める。
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        // secretの文字列からHMAC-SHA256用の鍵を生成
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // アクセストークンを発行する(userId, roleをペイロードに含める)
    public String createAccessToken(Long userId, String role) {
        return createToken(userId, role, accessTokenExpiration);
    }

    // リフレッシュトークンを発行する(有効期限が長い)
    public String createRefreshToken(Long userId, String role) {
        return createToken(userId, role, refreshTokenExpiration);
    }

    private String createToken(Long userId, String role, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // トークンが正しく、期限切れでないかを検証する
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            // 署名不正、期限切れ、フォーマット不正など、あらゆる検証失敗をここでまとめて弾く
            return false;
        }
    }

    // トークンからuserIdを取り出す
    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    // トークンからroleを取り出す
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}