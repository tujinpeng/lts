package com.github.ltsopensource.biz.logger.es;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.github.ltsopensource.biz.logger.domain.JobLoggerRequest;
import com.github.ltsopensource.json.JSONObject;

public class EsJobLoggerTest {

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
    	
    	System.out.println("searchCount:"+jobLogger.doPost("count",encodeMsg(map)));
    	System.out.println("searchResult:"+jobLogger.doPost("query",encodeMsg(map)));
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
