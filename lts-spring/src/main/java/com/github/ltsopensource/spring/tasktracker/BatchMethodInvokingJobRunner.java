package com.github.ltsopensource.spring.tasktracker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.github.ltsopensource.tasktracker.runner.JobRunner;

public class BatchMethodInvokingJobRunner implements InitializingBean {

	private Map<String, JobRunner> jobRunnerMap;

	public void init() {
		jobRunnerMap = new HashMap<String, JobRunner>();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (jobRunnerMap == null || jobRunnerMap.isEmpty()) {
			return;
		}
		Iterator<Map.Entry<String, JobRunner>> iter = jobRunnerMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, JobRunner> entry = iter.next();
			MethodInvokingJobRunner invoking = new MethodInvokingJobRunner();
			invoking.setShardValue(entry.getKey());
			invoking.setTargetMethod("run");
			invoking.setTargetObject(entry.getValue());
			invoking.afterPropertiesSet();
		}
	}

}
