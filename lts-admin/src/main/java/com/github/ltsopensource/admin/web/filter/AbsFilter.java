package com.github.ltsopensource.admin.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.util.AntPathMatcher;

import com.github.ltsopensource.admin.support.LoginConfigurer;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.utils.ServletUtil;
import com.lvmama.comm.vo.Constant;

/**
 * 抽象过滤器</br>
 * 注：类属性EXCLUDED_URL(无条件放行的URL)和NEED_AUTHENTICATE_URLS(需要授权的URL)不是互补关系
 * @author huqingyao
 * @version 1.0
 * @create 2016/10/25
 */
public abstract class AbsFilter implements Filter {
	/**
	 * 无授权弹窗的提示信息的key
	 */
	protected static final String AUTH_DIALOG_MSG = "auth.dialog.msg";
	
	/**
	 * 无条件放行的URL
	 */
	protected static List<String> EXCLUDED_URL = new ArrayList<String>();
	
	/**
	 * 需要授权的URL
	 */
	protected static List<String> NEED_AUTHENTICATE_URLS = new ArrayList<String>();
	
	/**
	 * 路径匹配工具类
	 */
	protected static final AntPathMatcher pathMatcher = new AntPathMatcher();
	
	/**
	 * 重定向的URl
	 */
	protected static final String REDIRECT_URL = "/pet_back/login.do";
	
	/**
	 * 过滤器初始化
	 * @param conf
	 * @throws ServletException
	 */
	public void init(FilterConfig conf) throws ServletException {
		loadExcludedUrls(conf);
		loadNeedAuthenticateUrls(conf);
	}
	
	/**
	 * 销毁过滤器
	 */
	public void destroy() {}
	
	/**
	 * 获取已登录用户
	 * @param request
	 * @param response
	 * @return
	 */
	protected PermUser getPermUser(HttpServletRequest request, HttpServletResponse response) {
		return (PermUser)ServletUtil.getSession(request, response, Constant.SESSION_BACK_USER);
	}
	
	/**
	 * 从配置文件加载无条件放行的URL
	 * @param conf
	 */
	private void loadExcludedUrls(FilterConfig conf) {
		String excludedUrls = conf.getInitParameter("excludedUrls");
		if(StringUtils.isNotEmpty(excludedUrls)){
			String[] urls = excludedUrls.split(",");
			for(String url : urls){
				EXCLUDED_URL.add(url.trim());
			}
		}
	}
	
	/**
	 * 从配置文件加载需要授权的URL
	 * @param conf
	 */
	private void loadNeedAuthenticateUrls(FilterConfig conf) {
		String needAuthenticateUrls = conf.getInitParameter("needAuthenticateUrls");
		if(StringUtils.isNotEmpty(needAuthenticateUrls)){
			String[] urls = needAuthenticateUrls.split(",");
			for(String url : urls){
				NEED_AUTHENTICATE_URLS.add(url.trim());
			}
		}
	}
	
	/**
	 * 是否为无条件放行的URL
	 * @param url
	 * @return
	 */
	protected boolean isExcludedUrl(String url) {
		if(StringUtils.isEmpty(url)){
			return false;
		}
		
		for(String eUrl : EXCLUDED_URL){
			if(pathMatcher.match(eUrl, url)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 是否为需要授权的URL
	 * @param url
	 * @return
	 */
	protected boolean isNeedAuthenticateUrl(String url) {
		if(StringUtils.isEmpty(url)){
			return false;
		}
		
		for(String nUrl : NEED_AUTHENTICATE_URLS){
			if(pathMatcher.match(nUrl, url)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 验证是否为配置文件中的授权用户
	 * @param request
	 * @param response
	 * @return
	 */
	protected boolean validateCfg(HttpServletRequest request, HttpServletResponse response) {
		PermUser user = this.getPermUser(request, response);
		if(null == user || StringUtils.isEmpty(user.getUserName())){
			return false;
		}
		
		return LoginConfigurer.containsUserName(user.getUserName());
	}
	
	/**
	 * 验证是否已在pet_back(super.lvmama.com)登录
	 * @param request
	 * @return true/false
	 */
	protected boolean validateSuper(HttpServletRequest request, HttpServletResponse response) {
		return null != this.getPermUser(request, response);
	}
	
	/**
	 * 弹窗提示
	 * @param response
	 * @param msg 提示信息
	 * @param isAjax 是否为ajax请求
	 * @throws IOException
	 */
	protected void showDialog(HttpServletResponse response, String msg, boolean isAjax) throws IOException {
		JSONObject json = new JSONObject();
		json.put("errno", 0);
		json.put("data", "alert('" + msg + "');");
		
		String contentType = isAjax ? "text/javascript; charset=utf-8" : "text/html; charset=utf-8";
		String out = isAjax ? json.toString() : "<script>alert('" + msg + "'); window.history.go(-1);</script>";
		
		response.setContentType(contentType);
		response.getWriter().write(out);
		response.getWriter().flush();
	}
	
	
}