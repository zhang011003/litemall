package org.linlinjava.litemall.wx.config;

import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.common.util.Consts;
import org.linlinjava.litemall.core.util.JacksonUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.linlinjava.litemall.db.util.AgentHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class AgentHandlerInterceptor extends HandlerInterceptorAdapter {

	private LitemallAdminService adminService;

	public AgentHandlerInterceptor(LitemallAdminService adminService) {
		this.adminService = adminService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String agent = request.getHeader(Consts.AGENT);
		if (!StringUtils.hasText(agent)) {
//            httpServletResponse.sendRedirect("/error/1");
			ServletOutputStream out = response.getOutputStream();
			byte[] bytes = JacksonUtil.toJson(ResponseUtil.agentError()).getBytes(StandardCharsets.UTF_8);
			response.setContentLength(bytes.length);
			out.write(bytes);
//            httpServletResponse.flushBuffer();
			return false;
		} else {
			List<LitemallAdmin> agentAdmin = adminService.findAdmin(agent);
			if (agentAdmin.size() <= 0) {
	//            httpServletResponse.sendRedirect("/error/1");
				ServletOutputStream out = response.getOutputStream();
				byte[] bytes = JacksonUtil.toJson(ResponseUtil.agentError()).getBytes(StandardCharsets.UTF_8);
				response.setContentLength(bytes.length);
				out.write(bytes);
				response.flushBuffer();
				return false;
			} else {
				AgentHolder.setAgent(agentAdmin.get(0));
			}
		}
		return true;
	}

}
