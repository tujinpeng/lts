package com.github.ltsopensource.jobclient.test;

import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSONObject;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.RetryJobClient;
import com.github.ltsopensource.jobclient.domain.Response;

public class TestJobClient {

	public static void main(String[] args) {
		
		//PropertyConfigurator.configure("E:\\work\\Git\\lts\\lts-jobclient\\src\\main\\resource\\log4j.properties");
		
		JobClient jobClient = new JobClient();
		jobClient.setNodeGroup("test_jobClient");
		jobClient.setClusterName("tnt_msg_cluster");
		jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
		jobClient.start();

		// 提交任务
		Job job = new Job();
		job.setTaskId("900000123");
		job.setParam("shopId", "12356");
		job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
		job.setNeedFeedback(false);
	    job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
	   
	    long start = System.currentTimeMillis();
	    Response response = jobClient.submitJob(job);
	    System.out.println(System.currentTimeMillis()-start);
	    System.out.println(response);
		// job.setCronExpression("0 0/1 * * * ?");  // 支持 cronExpression表达式
		// job.setTriggerTime(new Date()); // 支持指定时间执行
	    
//	    start = System.currentTimeMillis();
//	    response = jobClient.submitJob(job);
//	    System.out.println(System.currentTimeMillis()-start);
	    		
		
	}
	
}
