package com.synergizglobal.dms.config;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.synergizglobal.dms.common.JwtUtil;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.pmis.UserService;

@Component
public class SessionInterceptor implements HandlerInterceptor {

	@Autowired
	private UserService userService;
	@Override
	public boolean preHandle(HttpServletRequest request,
	                         HttpServletResponse response,
	                         Object handler) throws Exception {

	    HttpSession session = request.getSession(false);

	    if (session != null && session.getAttribute("user") != null) {
	        return true;
	    }

	    String token = request.getParameter("token");
	    if (token != null && !token.isEmpty()) {
	        String userId = JwtUtil.validateToken(token);
	        User user = userService.findById(userId).orElse(null);
	        if (user != null) {
	            if (session == null) {
	                session = request.getSession(true);
	            }
	            session.setAttribute("user", user);
	            return true;
	        }
	    }

	    boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

	    if (isAjax) {
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        response.setContentType("application/json");
	        response.getWriter().write("{\"error\": \"Unauthorized\"}");
	    } else {
	        response.sendRedirect(request.getContextPath() + "/error.html");
	    }

	    return false;
	}
}