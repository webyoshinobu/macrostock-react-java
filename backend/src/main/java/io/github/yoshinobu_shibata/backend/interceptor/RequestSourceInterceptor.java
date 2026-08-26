package io.github.yoshinobu_shibata.backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * リクエストごとに「HTTPメソッド + パス」(例: "POST /api/photos")を
 * ThreadLocalに保持するInterceptor。
 * JpaAuditingConfig の AuditorAware がこの値を取り出し、
 * created_by_source / updated_by_source カラムに反映する。
 */
@Component
public class RequestSourceInterceptor implements HandlerInterceptor {

    // スレッドごとに値を保持する領域。リクエストは基本的にスレッドを専有して処理されるため、
    // 「今処理中のリクエストの情報」を安全に保持できる
    private static final ThreadLocal<String> CURRENT_SOURCE = new ThreadLocal<>();

    // コントローラーの処理が始まる"前"に呼ばれるメソッド
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 例: "POST" + " " + "/api/photos" -> "POST /api/photos"
        String source = request.getMethod() + " " + request.getRequestURI();
        CURRENT_SOURCE.set(source);
        // true を返すと、後続の処理(コントローラーの実行)に進む
        return true;
    }

    // レスポンスが返り終わった"後"に必ず呼ばれるメソッド(例外が起きても呼ばれる)
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // ThreadLocalの後始末。消し忘れると、スレッドが使い回された際に
        // 別のリクエストへ前の値が漏れる可能性があるため必ず呼ぶ
        CURRENT_SOURCE.remove();
    }

    // 他のクラス(AuditorAware)から、今のリクエスト元情報を取得するための入り口
    public static String getCurrentSource() {
        return CURRENT_SOURCE.get();
    }
}