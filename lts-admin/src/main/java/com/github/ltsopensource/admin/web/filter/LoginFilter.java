package com.github.ltsopensource.admin.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录验证与授权：
 * 		验证01：是否为配置文件(lts-login.cfg)中用户已登录
 * 		验证02：是否已在pet_back(super.lvmama.com)登录
 * @author huqingyao
 * @version 1.0
 * @create 2016/10/25
 */
public class LoginFilter extends AbsFilter implements Filter {
	
	/**
	 * 过滤器业务处理
	 * @param req
	 * @param res
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		
		//是否为无条件放行的URL
		String url = request.getRequestURI();
		if(isExcludedUrl(url)){
			chain.doFilter(request, response);
			return;
		}
		
		//验证是否已登录
		if(validateSuper(request, response)){
			chain.doFilter(request, response);
			return;
		}
		
		response.sendRedirect(REDIRECT_URL);
	}
	
	
	
	
}