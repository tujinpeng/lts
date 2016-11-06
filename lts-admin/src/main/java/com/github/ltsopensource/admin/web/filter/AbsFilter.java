package com.github.ltsopensource.admin.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.springframework.util.AntPathMatcher;

import com.github.ltsopensource.admin.support.LoginConfigurer;
import com.github.ltsopensource.core.commons.utils.Base64;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.lvmama.comm.utils.ServletUtil;
import com.lvmama.tnt.comm.util.web.HttpServletLocalThread;
import com.lvmama.tnt.comm.vo.TntConstant;
import com.lvmama.tnt.user.po.TntUser;

/**
 * 抽象过滤器</br>
 * 注：类属性EXCLUDED_URL(无条件放行的URL)和NEED_AUTHENTICATE_URLS(需要授权的URL)不是互补关系
 * @author huqingyao
 * @version 1.0
 * @create 2016/10/25
 */
public abstract class AbsFilter implements Filter {
	/**
	 * WWW-authenticate授权前缀
	 */
	protected static final String AUTH_PREFIX = "Basic ";
	
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
	 * @param session
	 * @return
	 */
	protected TntUser getLoginUser(HttpSession session) {
		return TntUser.getTntUserByJson((String)getSession(TntConstant.SESSION_TNT_USER));
	}
	
	/**
	 * 根据key从session中查询对应的value
	 * @param key
	 * @return value
	 */
	protected Object getSession(String key) {
		if(null == key || "".equals(key.trim())){
			return null;
		}
		
		return ServletUtil.getSession(HttpServletLocalThread.getRequest(), HttpServletLocalThread.getResponse(), key);
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
	 * 验证是否为配置文件中用户已登录
	 * @param request
	 * @return true/false
	 */
	protected boolean validateCfg(HttpServletRequest request) {
		String auth = request.getHeader("authorization");
		if(null == auth || auth.length() < AUTH_PREFIX.length() || !auth.startsWith(AUTH_PREFIX)){
			return false;
		}
		
		//解码，明码格式为：用户名:密码
		String up = new String(Base64.decodeFast(auth.substring(AUTH_PREFIX.length(), auth.length())));
		if(!up.contains(":")){
			return false;
		}
		
		return up.split(":")[1].equals(LoginConfigurer.getProperty(up.split(":")[0]));
	}
	
	/**
	 * 验证是否已在pet_back(super.lvmama.com)登录
	 * @param request
	 * @return true/false
	 */
	protected boolean validateSuper(HttpServletRequest request) {
		return null != this.getLoginUser(request.getSession());
	}
	
	/**
	 * 验证通过，授权成功
	 * @param response
	 */
	protected void authenticateSuccess(HttpServletResponse response) {
        response.setStatus(200);
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
    }
	
	/**
	 * 验证失败，需重新验证
	 * @param response
	 */
	protected void needAuthenticate(HttpServletResponse response) {
        response.setStatus(401);
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("WWW-authenticate", AUTH_PREFIX + "Realm=\"lts user need auth\"");
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