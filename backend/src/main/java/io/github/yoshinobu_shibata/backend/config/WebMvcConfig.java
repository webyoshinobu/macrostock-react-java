package io.github.yoshinobu_shibata.backend.config;

import io.github.yoshinobu_shibata.backend.interceptor.RequestSourceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVCのInterceptor登録設定。
 * RequestSourceInterceptor を全APIリクエストに適用する。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RequestSourceInterceptor requestSourceInterceptor;

    // Spring MVCに「このInterceptorを、どのURLに対して使うか」を登録するメソッド
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestSourceInterceptor)
                // /api/ 配下のすべてのエンドポイントに適用
                .addPathPatterns("/api/**");
    }
}