package com.github.ltsopensource.admin.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.ltsopensource.admin.support.AppConfigurer;

/**
 * 访问权限(部分URL需授权用户才能访问)控制
 * @author huqingyao
 * @version 1.0
 * @create 2016/10/25
 */
public class AuthFilter extends AbsFilter implements Filter {
	
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
		
		String url = request.getRequestURI();
		
		//是否为无条件放行的URL OR 是否为不需要授权的URL OR 是否为已授权用户登录
		if(isExcludedUrl(url) || !isNeedAuthenticateUrl(url) || validateCfg(request)){
			chain.doFilter(request, response);
			return;
		}
		
		//提示用户对当前请求的URL不具有操作权限
		String reqType = request.getHeader("X-Requested-With");
		boolean isAjax = (null != reqType && reqType.equals("XMLHttpRequest")); //是否为ajax请求
		showDialog(response, AppConfigurer.getProperty(AUTH_DIALOG_MSG), isAjax);
	}
	
	
}