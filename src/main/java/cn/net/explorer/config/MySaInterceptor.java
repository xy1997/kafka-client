package cn.net.explorer.config;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;


public class MySaInterceptor extends SaInterceptor {

    /**
     * 续签时间(S)
     */
    private Integer timeoutSeconds;
    public MySaInterceptor setTimeoutSeconds(Integer timeoutSeconds){
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public MySaInterceptor(SaParamFunction<Object> auth) {
        super(auth);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ( Objects.equals(request.getMethod(),"OPTIONS")) {
            return true;
        }

        //sa-token续签
        if (StpUtil.isLogin()) StpUtil.renewTimeout(timeoutSeconds);

        try {
            if (this.isAnnotation && handler instanceof HandlerMethod) {
                Method method = ((HandlerMethod) handler).getMethod();
                if ((Boolean) SaStrategy.me.isAnnotationPresent.apply(method, SaIgnore.class)) {
                    return true;
                }

                SaStrategy.me.checkMethodAnnotation.accept(method);
            }

            this.auth.run(handler);
        } catch (StopMatchException var5) {
        } catch (BackResultException var6) {
            if (response.getContentType() == null) {
                response.setContentType("text/plain; charset=utf-8");
            }

            response.getWriter().print(var6.getMessage());
            return false;
        }

        return true;
    }

}
