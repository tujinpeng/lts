package com.github.ltsopensource.admin.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.ltsopensource.core.commons.utils.StringUtils;

/**
 * 加载用于登录的用户名/密码的配置文件
 * @author huqingyao
 * @version 1.0
 * @create 2016/10/25
 */
public class LoginConfigurer {
	
	private static AtomicBoolean load = new AtomicBoolean(false);
	
	/**
	 * 配置文件名称
	 */
	private static final String CONF_LOGIN_NAME = "lts-login.cfg";
	
	/**
	 * 存储配置文件内容集合</br>
	 * 用户名
	 */
	private static final List<String> LTS_USERNAME = new ArrayList<String>();
	
	/**
	 * 系统初始化-读取配置文件
	 */
	public static void load(String confPath) {
		String path = "";
		try {
			if(load.compareAndSet(false, true)){
				Properties conf = new Properties();
				
				if(StringUtils.isNotEmpty(confPath)){
					path = confPath + "/" + CONF_LOGIN_NAME;
					InputStream is = new FileInputStream(new File(path));
					conf.load(is);
				}else{
					path = CONF_LOGIN_NAME;
					InputStream is = AppConfigurer.class.getClassLoader().getResourceAsStream(path);
					conf.load(is);
				}
				
				for(Map.Entry<Object, Object> entry : conf.entrySet()){
					if(null == entry.getKey() || null == entry.getValue()){
	    				continue;
	    			}
	    			
					LTS_USERNAME.add(entry.getKey().toString());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Load lts-login.cfg, path[" + path + "] error.", e);
		}
	}
	
	/**
	 * 是否包含目标用户名
	 * @param name 目标用户名
	 * @return true/false
	 */
	public static boolean containsUserName(String name) {
		return LTS_USERNAME.contains(name);
	}
	
	
}