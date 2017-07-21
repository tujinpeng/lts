package com.github.ltsopensource.startup.tasktracker;

import com.github.ltsopensource.startup.tasktracker.test.TestJobRunner;
import com.github.ltsopensource.tasktracker.TaskTracker;

public class TestTaskTracker {

	public static void main(String[] args) {

		TaskTracker taskTracker = new TaskTracker();
		taskTracker.setJobRunnerClass(TestJobRunner.class);
		taskTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
		taskTracker.setNodeGroup("test_trade_TaskTracker");
		taskTracker.setClusterName("tnt_msg_cluster");
//		System.out.println("work thread:"+args[0]);
//		taskTracker.setWorkThreads(Integer.parseInt(args[0]));
//		taskTracker.setPullRate(Integer.parseInt(args[1]));
//		System.out.println("pull rate:"+args[1]);
		taskTracker.setWorkThreads(30);
		taskTracker.setPullRate(1000);
		taskTracker.start();
		
		
	}
	
}
