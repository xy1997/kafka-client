package cn.net.explorer.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.net.explorer.domain.IgnoreWhiteProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Resource
    private IgnoreWhiteProperties ignoreWhiteProperties;

    @Value("${sa-token.timeout}")
    private Integer timeout;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new MySaInterceptor(handle -> StpUtil.checkLogin()).setTimeoutSeconds(timeout))
                .addPathPatterns("/**")
                .excludePathPatterns(ignoreWhiteProperties.getWhites());
    }
}
