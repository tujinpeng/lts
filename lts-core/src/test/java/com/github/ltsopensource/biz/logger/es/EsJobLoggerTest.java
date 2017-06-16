package com.github.ltsopensource.biz.logger.es;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.alibaba.fastjson.JSON;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.JobLoggerRequest;
import com.github.ltsopensource.json.JSONObject;

public class EsJobLoggerTest extends TestCase {

	private EsJobLogger logger = new EsJobLogger(null);
	
	public void testSave() {
		
		JobLogPo log = new JobLogPo();
		log.setRealTaskId("991000014512");
		log.setBizId("0000");
		log.setBizType("saveOneBizType");
		log.setEventType("saveOneEventType");
		log.setLogTime(new Date().getTime());
		log.setMsg("saveOne.msg.test");
		log.setNeedFeedback(true);
		log.setPriority(5);
    	
    	logger.log(log);
	
		CountDownLatch latch = new CountDownLatch(1);
		try {
			latch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
	public void testBatchSave() {
		
		List<JobLogPo> jobLogPos = new ArrayList<JobLogPo>();
		
		for(int i = 0; i < 3; i++) {
			
			JobLogPo log = new JobLogPo();
			log.setRealTaskId("9910000145"+i);
			log.setBizId("0000");
			log.setBizType("saveOneBizType");
	    	log.setEventType("saveOneEventType");
	    	log.setLogTime(new Date().getTime());
	    	log.setMsg("saveOne.msg.test");
	    	log.setNeedFeedback(true);
	    	log.setPriority(5);
			jobLogPos.add(log);
			
		}
		
		EsJobLogger logger = new EsJobLogger(null);
		logger.log(jobLogPos);
	
		CountDownLatch latch = new CountDownLatch(1);
		try {
			latch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void testQuery() {
		
		EsJobLogger logger = new EsJobLogger(null);
		JobLoggerRequest request = new JobLoggerRequest();
		request.setStart(0);
		request.setLimit(10);
		request.setRealTaskId("991000014512");
		
		PaginationRsp<JobLogPo> result = logger.search(request);
		System.out.println(JSON.toJSON(result));
		
	}
	
	public void testFuse() {
		
		ThreadPoolExecutor executer = new ThreadPoolExecutor(20, 100, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
		
		for(int i = 0; i< 200; i++) {
			
			executer.submit(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					testSave();
				}
			});

		}
		
		CountDownLatch latch = new CountDownLatch(1);
		try {
			latch.await(50000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/**
     * test method
	 * 要测试dubbo-rest，需要将http://10.200.3.7/svn/soa/trunk/dubbo-rest拉取到本地启动下(检查下zk注册地址是否和需要调用的dubbo服务注册地址相同)
     * @param args
     */
    public static void main(String[] args) throws UnsupportedEncodingException {    	    	
    	EsJobLogger jobLogger = new EsJobLogger(null);
    	/*List<JobLogPo> jobs = new ArrayList<JobLogPo>();
    	JobLogPo jlp1 = new JobLogPo();
    	jlp1.setBizId("0000");
    	jlp1.setBizType("saveOneBizType");
    	jlp1.setEventType("saveOneEventType");
    	jlp1.setLogTime(new Date().getTime());
    	jlp1.setMsg("saveOne.msg.test");
    	jlp1.setNeedFeedback(true);
    	jlp1.setPriority(5);
    	
    	JobLogPo jlp2 = new JobLogPo();
    	jlp2.setBizId("8888");
    	jlp2.setBizType("2test");
    	jlp2.setEventType("2test");
    	jlp2.setLogTime(new Date().getTime());
    	jlp2.setMsg("lts.saveAll.test");
    	jlp2.setNeedFeedback(true);
    	jlp2.setPriority(5);
    	
    	JobLogPo jlp3 = new JobLogPo();
    	jlp3.setBizId("8888");o
    	jlp3.setBizType("btest");
    	jlp3.setEventType("etest");
    	jlp3.setLogTime(new Date().getTime());
    	jlp3.setMsg("lts.saveAll.test");
    	jlp3.setNeedFeedback(true);
    	jlp3.setPriority(5);
    	
    	jobs.add(jlp1);
    	System.out.println("sendMsg:"+encodeMsg(jlp1));
    	System.out.println("saveResult:"+doPost("saveOne",encodeMsg(jlp1)));*/
    	
    	/*jobs.add(jlp2);
    	jobs.add(jlp3);    	
    	System.out.println("saveResult:"+doPost("saveAll",encodeMsg(jobs)));*/
    	
    	
    	JobLoggerRequest jlr = new JobLoggerRequest();
    	//jlr.setTaskId("9999");
    	//jlr.setBizId("20042581");
    	//jlr.setEventType("etest");
    	Calendar c = Calendar.getInstance();
    	c.add(Calendar.DAY_OF_MONTH,-3);
    	//jlr.setStartLogTimeMill(c.getTimeInMillis());
    	//jlr.setEndLogTimeMill(new Date().getTime());
    	
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("pageSize",jlr.getLimit());
    	map.put("pageNum",0);
    	map.put("parameter",jlr);
    	System.out.println("searchParam:"+JSONObject.toJSONString(map));
    	
    	try {
			System.out.println("searchCount:"+jobLogger.submitSync("count",encodeMsg(map)));
			System.out.println("searchResult:"+jobLogger.submitSync("query",encodeMsg(map)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    private static String encodeMsg(Object oj){
    	if(null==oj){
    		return "";
    	}
    	String result = "";
    	try {
    		//json-->encode
    		result = URLEncoder.encode(JSONObject.toJSONString(oj),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return result;
    }    
}
