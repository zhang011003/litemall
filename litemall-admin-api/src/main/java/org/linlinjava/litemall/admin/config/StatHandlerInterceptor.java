package org.linlinjava.litemall.admin.config;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
public class StatHandlerInterceptor extends HandlerInterceptorAdapter {
	
	private static ThreadLocal<Map<String, StopWatch>> stopWatchThreadLocal = ThreadLocal.withInitial(() -> Maps.newHashMap());
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		log.debug("Begin to invoke {}, params:{}", request.getRequestURI(), request.getQueryString());
		if (!request.getRequestURI().equals("")) {
			StopWatch stopWatch = new StopWatch(request.getRequestURI());
			stopWatchThreadLocal.get().put(request.getRequestURI(), stopWatch);
			stopWatch.start();
		}

//		if (request.getSession().getAttribute("SPRING_SECURITY_CONTEXT") == null) {
//			if (request.getHeader("X-requested-with") != null
//					&& request.getHeader("X-requested-with").equalsIgnoreCase("XMLHttpRequest")){
//				response.setHeader("sessionstatus", "timeout");
//			}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		StopWatch stopWatch = stopWatchThreadLocal.get().remove(request.getRequestURI());
		if (stopWatch != null && stopWatch.isRunning()) {
			stopWatch.stop();
			log.debug(stopWatch.shortSummary());
		}
		log.debug("End to invoke {}, params:{}", request.getRequestURI(), request.getQueryString());
	}

}
