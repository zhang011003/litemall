package org.linlinjava.litemall.admin.config;

import org.linlinjava.litemall.admin.annotation.annotation.support.AdminLoginUserHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Component
public class AdminWebMvcConfig implements WebMvcConfigurer {

    public StatHandlerInterceptor statHandlerInterceptor() {
        return new StatHandlerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(statHandlerInterceptor());
        registration.addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AdminLoginUserHandlerMethodArgumentResolver());
    }
}
