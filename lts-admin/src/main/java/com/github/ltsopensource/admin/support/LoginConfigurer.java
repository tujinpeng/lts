package com.github.ltsopensource.admin.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 加载用于登录的用户名/密码的配置文件
 * @author huqingyao
 * @version 1.0
 * @create 2016/10/25
 */
public class LoginConfigurer {
	/**
	 * 配置文件名称
	 */
	private static final String CONF_LOGIN_NAME = "lts-login.cfg";
	
	/**
	 * 存储配置文件内容集合</br>
	 * key/val : 用户名/密码
	 */
	private static final Map<String, String> LTS_USERNAME_PASSWORD = new HashMap<String, String>();
	
    /**
     * 系统初始化-读取配置文件
     */
    public static void load() {
    	try {
    		InputStream is = LoginConfigurer.class.getClassLoader().getResourceAsStream(CONF_LOGIN_NAME);
    		
    		Properties conf = new Properties();
    		conf.load(is);
    		
    		for(Map.Entry<Object, Object> entry : conf.entrySet()) {
    			if(null == entry.getKey() || null == entry.getValue()){
    				continue;
    			}
    			
    			LTS_USERNAME_PASSWORD.put(entry.getKey().toString(), entry.getValue().toString());
    		}
    	} catch (IOException e) {
    		throw new RuntimeException("Load [" + CONF_LOGIN_NAME + "] error!", e);
    	}
    }
    
    /**
     * 根据用户名获取对应的密码值
     * @param name 用户名
     * @return 密码值
     */
    public static String getProperty(String name) {
    	return LTS_USERNAME_PASSWORD.get(name);
    }
    
    
    
}