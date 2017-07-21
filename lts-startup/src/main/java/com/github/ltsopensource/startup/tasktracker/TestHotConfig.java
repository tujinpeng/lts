package com.github.ltsopensource.startup.tasktracker;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.zookeeper.ZkClient;
import com.github.ltsopensource.zookeeper.zkclient.ZkClientZkClient;

public class TestHotConfig {

//	@Test
	public void test() {
		
		
		Config config = new Config();
		config.setRegistryAddress("zookeeper://127.0.0.1:2181");
		
		ZkClient zkClient = new ZkClientZkClient(config);
		
	    zkClient.setData("/tnt_msg_cluster/JOB_TRACKER/lts/lts.job.processor.thread", 30);
	    
	    System.out.println();
		
	}
}
