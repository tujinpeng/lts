package com.github.ltsopensource.biz.logger.es.util;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class HttpClientFactory {

	public static DefaultHttpClient httpClient;
	
	static {
		
		HttpParams httpParams = new BasicHttpParams(); 
	    HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1); 
	    HttpProtocolParams.setUserAgent(httpParams, "lts-agent");  	  

		httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2*1000);  
		httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 2*1000); 
	  
		SchemeRegistry schreg = new SchemeRegistry();  
		schreg.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory())); 

		PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(500);
		connectionManager.setMaxTotal(500);   
	
		httpClient = new DefaultHttpClient(connectionManager, httpParams);
		httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
		
	}
	
	private HttpClientFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static HttpClient getHttpClient() {
		return httpClient;
	}
	
}
