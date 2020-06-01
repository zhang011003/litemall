package org.linlinjava.litemall.admin.annotation.annotation.support;

import org.apache.shiro.SecurityUtils;
import org.linlinjava.litemall.admin.annotation.annotation.AdminLoginUser;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


public class AdminLoginUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.getParameterType().isAssignableFrom(Integer.class)
                    || parameter.getParameterType().isAssignableFrom(LitemallAdmin.class))
                && parameter.hasParameterAnnotation(AdminLoginUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory factory) throws Exception {

//        return new Integer(1);
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        if (parameter.getParameterType().isAssignableFrom(Integer.class)) {
            return litemallAdmin.getId();
        } else if (parameter.getParameterType().isAssignableFrom(LitemallAdmin.class)) {
            return litemallAdmin;
        }
        return null;
    }
}
