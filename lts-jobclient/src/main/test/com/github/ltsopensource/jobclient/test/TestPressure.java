package com.github.ltsopensource.jobclient.test;

import org.apache.log4j.PropertyConfigurator;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.domain.Response;

public class TestPressure {

	public static void main(String[] args) {
		
		PropertyConfigurator.configure("E:\\log4j.properties");
		
		final TestPressure testPressure = new TestPressure();

		for(int i = 0; i < 100; i++) {
			
			final JobClient jobClient = new JobClient();
			jobClient.setNodeGroup("test_jobClient");
			jobClient.setClusterName("tnt_msg_cluster");
			jobClient.setRegistryAddress("zookeeper://10.200.6.198:2181");
			jobClient.start();
			
			final int sorta = i;
			Thread work = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					int sortb = 0;
					while(true) {
						testPressure.sumit(jobClient, sorta+"_"+sortb++);
					}
				}
			});
			
			work.start();
			
		}
		
		
		
		
		
	}
	
	
	public void sumit(JobClient jobClient, String sort) {
		
		// 提交任务
		Job job = new Job();
		job.setTaskId("900000123_"+sort);
		job.setParam("shopId", "12356");
		job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
		job.setNeedFeedback(false);
	    job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
	   
	    long start = System.currentTimeMillis();
	    Response response = jobClient.submitJob(job);
	    System.out.println(System.currentTimeMillis()-start);
	    System.out.println(response);
		
	    
	}
	
	
	
	
}
