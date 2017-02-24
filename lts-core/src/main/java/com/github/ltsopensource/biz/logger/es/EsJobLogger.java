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


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.biz.logger.JobLogger;
import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.JobLoggerRequest;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.json.JSONObject;
/**
 * 分销LTS日志操作
 * @author haofeifei
 *
 */
public class EsJobLogger implements JobLogger {

	Logger log = Logger.getLogger(EsJobLogger.class);
	
	public static final String CHARACTER_ENCODING = "UTF-8";
	
    public EsJobLogger(Config config) {
       
    }    
    /**
     * send post
     * @param method
     * @param log
     * @return
     */
    public static String doPost(String method,String log){
		//String urlAPI = "http://localhost:8081/dubbo-rest/generic/com.lvmama.bigger.biz.service.IESLtsSyncLogService/"+method;
		String urlAPI = "http://super.lvmama.com/dubbo-rest/generic/com.lvmama.prism.biz.esser.IESLtsSyncLogService/"+method;//Post方式没有参数在这里
		//String urlAPI = "http://10.200.4.53:8090/dubbo-rest/generic/com.lvmama.bigger.biz.service.IESLtsSyncLogService/"+method;//ark
        String result = "";
        HttpPost httpRequst = new HttpPost(urlAPI);//创建HttpPost对象
        
        List <NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("isPassedBy", "true"));

        List<Map<String, Object>> args = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        //map.put("type", "com.lvmama.prism.biz.esentity.TntLtsLog");
        map.put("type","java.lang.String");
        map.put("value",log);
        
        args.add(map);
        //params.add(new BasicNameValuePair("arguments",JSON.toJSONString(args)));
        params.add(new BasicNameValuePair("arguments",JSON.toJSONString(args)));

        try {
            httpRequst.setEntity(new UrlEncodedFormEntity(params,CHARACTER_ENCODING));
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequst);
            if(httpResponse.getStatusLine().getStatusCode() == 200)
            {
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity);//取出应答字符串
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = e.getMessage().toString();
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
            result = e.getMessage().toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage().toString();
        }finally{
        	httpRequst.releaseConnection();
        }
        return result;
    }
    /**
     *save one log 
     */
    @Override
    public void log(JobLogPo jobLogPo) {
        if (jobLogPo == null) {
            return;
        }
        //String object = JSONObject.toJSONString(jobLogPo);
        doPost("saveOne", encodeMsg(jobLogPo));
    }
    
    /**
     * save more log
     */
    @Override
    public void log(List<JobLogPo> jobLogPos) {
        if (CollectionUtils.isEmpty(jobLogPos)) {
            return;
        }
        //String object = com.alibaba.fastjson.JSON.toJSON(jobLogPos).toString();
        doPost("saveAll", encodeMsg(jobLogPos));
    }
    /**
     * search log informations
     */
    @Override
    public PaginationRsp<JobLogPo> search(JobLoggerRequest request) {
        PaginationRsp<JobLogPo> response = new PaginationRsp<JobLogPo>();
        
        Map<String,Object> map = new HashMap<String,Object>();
    	map.put("pageSize",request.getLimit());
    	//eg:第一页(0,10)传送0，第二页(20,10)传送1....
    	Integer pageNum = request.getStart();
    	if(pageNum!=0){
    		if(0==request.getStart()/request.getLimit()){
    			pageNum = request.getStart()/request.getLimit()-1;
    		}else{
    			pageNum = request.getStart()/request.getLimit();
    		}    		
    	}
    	map.put("pageNum",pageNum);//pageNum
    	map.put("parameter",request);
    	if(null!=request.getStartLogTime() && null!=request.getEndLogTime()){
    		request.setStartLogTimeMill(request.getStartLogTime().getTime());
    		request.setEndLogTimeMill(request.getEndLogTime().getTime());
    	}
    	
        //String objectCount = JSONObject.toJSONString(request);
        String count = doPost("count", encodeMsg(map));
        if (null == count || count.isEmpty() || Long.valueOf(count) == 0L) {
            return response;
        }
        int cut = Long.valueOf(count).intValue();
        response.setResults(cut);
        
        //String objectData = JSONObject.toJSONString(request);
		String searchResult = doPost("query", encodeMsg(map));
		
		List<JobLogPo> rows = new ArrayList<JobLogPo>();
		if(StringUtils.isEmpty(searchResult)){
			response.setRows(rows);
			return response;
		}
		//List<JobLogPo> jobLogPoList= com.alibaba.fastjson.JSONObject.parseArray(searchResult,JobLogPo.class);
		List<?> jobLogPoList= net.sf.json.JSONArray.fromObject(searchResult);
		if (jobLogPoList!=null && !jobLogPoList.isEmpty()) {
			for (Object o : jobLogPoList) {
				JobLogPo po =JSONObject.parseObject(String.valueOf(o),JobLogPo.class);
				rows.add(po);
			}
		}
		response.setRows(rows);
        return response;
    }
    private static String encodeMsg(Object oj){
    	if(null==oj){
    		return "";
    	}
    	String result = "";
    	try {
    		//json-->encode
    		result = URLEncoder.encode(JSONObject.toJSONString(oj),CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return result;
    }    
    /**
     * test method
	 * 要测试dubbo-rest，需要将http://10.200.3.7/svn/soa/trunk/dubbo-rest拉取到本地启动下(检查下zk注册地址是否和需要调用的dubbo服务注册地址相同)
     * @param args
     */
    public static void main(String[] args) throws UnsupportedEncodingException {    	    	
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
    	jlp3.setBizId("8888");
    	jlp3.setBizType("btest");
    	jlp3.setEventType("etest");
    	jlp3.setLogTime(new Date().getTime());
    	jlp3.setMsg("lts.saveAll.test");
    	jlp3.setNeedFeedback(true);
    	jlp3.setPriority(5);
    	
    	jobs.add(jlp1);
    	System.out.println("sendMsg:"+encodeMsg(jlp1));
    	System.out.println("saveResult:"+doPost("saveOne",encodeMsg(jlp1)));
    	
    	/*jobs.add(jlp2);
    	jobs.add(jlp3);    	
    	System.out.println("saveResult:"+doPost("saveAll",encodeMsg(jobs)));*/
    	
    	
    	JobLoggerRequest jlr = new JobLoggerRequest();
    	jlr.setTaskId("3333");
    	//jlr.setBizId("0000");
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
    	
    	System.out.println("searchCout:"+doPost("count",encodeMsg(map)));
    	System.out.println("searchResult:"+doPost("query",encodeMsg(map)));
	}
   
    /**
    protected void copyProperties(JobLogPo jobLogPo,TntLtsLogVo tntLtsLogVo){
    	tntLtsLogVo.setLog_time(String.valueOf(jobLogPo.getLogTime()));
    	tntLtsLogVo.setGmt_created(String.valueOf(jobLogPo.getGmtCreated()));
    	tntLtsLogVo.setLog_type(jobLogPo.getLogType().name());
    	tntLtsLogVo.setSuccess(String.valueOf(jobLogPo.isSuccess()));
    	tntLtsLogVo.setMsg(jobLogPo.getMsg());
    	tntLtsLogVo.setTask_tracker_identity(jobLogPo.getTaskTrackerIdentity());
    	tntLtsLogVo.setLevel(jobLogPo.getLevel().name());
    	tntLtsLogVo.setTask_id(jobLogPo.getTaskId());
    	tntLtsLogVo.setReal_task_id(jobLogPo.getRealTaskId());
    	tntLtsLogVo.setJob_id(jobLogPo.getJobId());
    	tntLtsLogVo.setJob_type(jobLogPo.getJobType() == null ? null : jobLogPo.getJobType().name());
    	tntLtsLogVo.setPriority(String.valueOf(jobLogPo.getPriority()));
    	tntLtsLogVo.setSubmit_node_group(jobLogPo.getSubmitNodeGroup());
    	tntLtsLogVo.setTask_tracker_node_group(jobLogPo.getTaskTrackerNodeGroup());
    	tntLtsLogVo.setExt_params(JSON.toJSONString(jobLogPo.getExtParams()));
    	tntLtsLogVo.setInternal_ext_params(JSON.toJSONString(jobLogPo.getInternalExtParams()));
    	tntLtsLogVo.setNeed_feedback(String.valueOf(jobLogPo.isNeedFeedback()));
    	tntLtsLogVo.setCron_expression(jobLogPo.getCronExpression());
    	tntLtsLogVo.setTrigger_time(String.valueOf(jobLogPo.getTriggerTime()));
    	tntLtsLogVo.setRetry_times(String.valueOf(jobLogPo.getRetryTimes()));
    	tntLtsLogVo.setMax_retry_times(String.valueOf(jobLogPo.getMaxRetryTimes()));
    	tntLtsLogVo.setRely_on_prev_cycle(String.valueOf(jobLogPo.getDepPreCycle()));
    	tntLtsLogVo.setRepeat_count(String.valueOf(jobLogPo.getRepeatCount()));
    	tntLtsLogVo.setRepeated_count(String.valueOf(jobLogPo.getRepeatedCount()));
    	tntLtsLogVo.setRepeat_interval(String.valueOf(jobLogPo.getRepeatInterval()));
    	tntLtsLogVo.setEventType(jobLogPo.getExtParams().get("eventType"));
    	tntLtsLogVo.setBizId(jobLogPo.getExtParams().get("bizId"));
    	tntLtsLogVo.setBizType(jobLogPo.getExtParams().get("bizType"));
    }
    
    protected void copyProperties(TntLtsLogVo tntLtsLogVo,JobLogPo jobLogPo){
    	jobLogPo.setLogTime(Long.valueOf(tntLtsLogVo.getLog_time()));
    	jobLogPo.setGmtCreated(Long.valueOf(tntLtsLogVo.getGmt_created()));
    	jobLogPo.setLogType(tntLtsLogVo.getLog_type()== null ? null : Enum.valueOf(LogType.class, tntLtsLogVo.getLog_type()));
    	jobLogPo.setSuccess(Boolean.parseBoolean(tntLtsLogVo.getSuccess()));
    	jobLogPo.setMsg(tntLtsLogVo.getMsg());
    	jobLogPo.setTaskTrackerIdentity(tntLtsLogVo.getTask_tracker_identity());
    	jobLogPo.setLevel(tntLtsLogVo.getLevel() == null ? null: Enum.valueOf(Level.class, tntLtsLogVo.getLevel()));
    	jobLogPo.setTaskId(tntLtsLogVo.getTask_id());
    	jobLogPo.setRealTaskId(tntLtsLogVo.getReal_task_id());
    	jobLogPo.setJobId(tntLtsLogVo.getJob_id());
    	jobLogPo.setJobType(tntLtsLogVo.getJob_type() == null ? null : Enum.valueOf(JobType.class, tntLtsLogVo.getJob_type()));
    	jobLogPo.setPriority(tntLtsLogVo.getPriority()== null ? null : Integer.parseInt(tntLtsLogVo.getPriority()));
    	jobLogPo.setSubmitNodeGroup(tntLtsLogVo.getSubmit_node_group());
    	jobLogPo.setTaskTrackerNodeGroup(tntLtsLogVo.getTask_tracker_node_group());
    	jobLogPo.setExtParams((Map)JSON.toJSONObject(tntLtsLogVo.getExt_params()));
    	jobLogPo.setInternalExtParams((Map)JSON.toJSONObject(tntLtsLogVo.getInternal_ext_params()));
    	jobLogPo.setNeedFeedback(Boolean.parseBoolean(tntLtsLogVo.getNeed_feedback()));
    	jobLogPo.setCronExpression(tntLtsLogVo.getCron_expression());
    	jobLogPo.setTriggerTime(Long.valueOf(tntLtsLogVo.getTrigger_time()));
    	jobLogPo.setRetryTimes(tntLtsLogVo.getRetry_times()==null ? null: Integer.parseInt(tntLtsLogVo.getRetry_times()));
    	jobLogPo.setMaxRetryTimes(tntLtsLogVo.getMax_retry_times() == null ? null: Integer.parseInt(tntLtsLogVo.getMax_retry_times()));
    	jobLogPo.setDepPreCycle(Boolean.parseBoolean(tntLtsLogVo.getRely_on_prev_cycle()));
    	jobLogPo.setRepeatCount(tntLtsLogVo.getRepeat_count() ==null ? null: Integer.parseInt(tntLtsLogVo.getRepeat_count()));
    	jobLogPo.setRepeatedCount(tntLtsLogVo.getRepeated_count()==null ? null : Integer.parseInt(tntLtsLogVo.getRepeated_count()));
    	jobLogPo.setRepeatInterval(Long.valueOf(tntLtsLogVo.getRepeat_interval()));
    	Map<String, String> map =new  HashMap<String, String>();
    	map.put("eventType", tntLtsLogVo.getEventType());
    	map.put("bizId", tntLtsLogVo.getBizId());
    	map.put("bizType", tntLtsLogVo.getBizType());
    	jobLogPo.setExtParams(map);
    }*/
}
