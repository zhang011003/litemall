package org.linlinjava.litemall.wx.config;

import org.linlinjava.litemall.common.config.CommonConfig;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.linlinjava.litemall.wx.annotation.support.LoginUserHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WxWebMvcConfiguration implements WebMvcConfigurer {
    @Autowired
    private LitemallAdminService adminService;
    @Autowired
    private CommonConfig commonConfig;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new LoginUserHandlerMethodArgumentResolver());
    }

    public StatHandlerInterceptor statHandlerInterceptor() {
        return new StatHandlerInterceptor();
    }

    public AgentHandlerInterceptor agentHandlerInterceptor() {
        return new AgentHandlerInterceptor(adminService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(statHandlerInterceptor());
        registration.addPathPatterns("/wx/**");

        if (commonConfig.supportGoodsDistribution()) {
            InterceptorRegistration registration2 = registry.addInterceptor(agentHandlerInterceptor());
            registration2.addPathPatterns("/wx/**");
        }
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/*")
//                .allowCredentials(true)
//                .allowedHeaders("*")
//                .allowedMethods("*")
//                .allowedOrigins("*")
//                .exposedHeaders(HttpHeaders.SET_COOKIE).maxAge(3600L);
//    }
}
