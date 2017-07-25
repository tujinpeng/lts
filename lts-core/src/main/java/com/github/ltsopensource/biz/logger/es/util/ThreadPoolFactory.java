package com.github.ltsopensource.biz.logger.es.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolFactory {

	private static ThreadPoolExecutor pool;
	
	static {
		pool = new ThreadPoolExecutor(25, 500, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(500));
	}
	
	private ThreadPoolFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static ThreadPoolExecutor getThreadPool() {
		return pool;
	}
	
	
	
}
