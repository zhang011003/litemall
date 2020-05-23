package org.linlinjava.litemall.wx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WxWebMvcConfig implements WebMvcConfigurer {

    public StatHandlerInterceptor statHandlerInterceptor() {
        return new StatHandlerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(statHandlerInterceptor());
        registration.addPathPatterns("/wx/**");
    }
}
