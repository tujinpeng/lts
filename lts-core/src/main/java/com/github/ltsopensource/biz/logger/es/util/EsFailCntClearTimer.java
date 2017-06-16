package com.github.ltsopensource.biz.logger.es.util;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class EsFailCntClearTimer extends TimerTask {
	
	private AtomicInteger failCount;
	
	public EsFailCntClearTimer(AtomicInteger failCount) {
		// TODO Auto-generated constructor stub
		this.failCount = failCount;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		failCount.getAndSet(0);
	}

	
	
}
