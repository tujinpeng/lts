package com.github.ltsopensource.biz.logger.es;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.alarm.email.EmailAlarmMessage;
import com.github.ltsopensource.alarm.email.EmailAlarmNotifier;
import com.github.ltsopensource.biz.logger.JobLogger;
import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.JobLoggerRequest;
import com.github.ltsopensource.biz.logger.es.annotation.EsLogFilter;
import com.github.ltsopensource.biz.logger.es.annotation.EsLogFilter.Optype;
import com.github.ltsopensource.biz.logger.es.util.HttpClientFactory;
import com.github.ltsopensource.biz.logger.es.util.ThreadPoolFactory;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

public class EsJobLogger implements JobLogger
{
	
	private Logger logger = LoggerFactory.getLogger(EsJobLogger.class);
	
	private static String ES_URL;
	
	private AtomicInteger failCount = new AtomicInteger(0);
	
	private AtomicBoolean isShutDown = new AtomicBoolean();
	
	private Timer timer = new Timer();
	
	private TimerTask failCntClearTask;
	
	private final String Delimiters = "\n";
	
	private EmailAlarmNotifier alarmNotifier;
	
	private String alarmEmail;
	
	private String nodeAddress;
	
	private enum EsOpType {
		
		SAVE(""),
		BULKSAVE("_bulk"),
		COUNT("_count"),
		SEARCH("_search"),
		INDEX("index");
		
		private String value;
		
		private EsOpType(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
	}
	
	public EsJobLogger(Config config) {
		
		ES_URL = config.getParameter("es.log.url");

		alarmNotifier = new EmailAlarmNotifier(config);
		
		alarmEmail = config.getParameter(ExtConfig.ES_ALARM_EMAIL);
		
		nodeAddress = config.getIp();
		
		startFixedRateClean();
    
	}

	@Override
    public void log(JobLogPo jobLogPo) {
        
		if (jobLogPo == null) {
            return;
        }
        
        if(jobLogPo.getExtParams() != null){
        	jobLogPo.setBizId(jobLogPo.getExtParams().get("bizId"));
        	jobLogPo.setBizType(jobLogPo.getExtParams().get("bizType"));
        	jobLogPo.setEventType(jobLogPo.getExtParams().get("eventType"));
        }
        
        submitAsync(EsOpType.SAVE.getValue(), JSON.toJSONString(jobLogPo));
    
	}
    
    @Override
    public void log(List<JobLogPo> jobLogPos) {
        
    	if (CollectionUtils.isEmpty(jobLogPos)) {
            return;
        }
    	
    	StringBuilder builder = new StringBuilder();
        JSONObject create = new JSONObject();
        create.put(EsOpType.INDEX.getValue(), new JSONObject());
        
        for(JobLogPo jp: jobLogPos){
        	
        	builder.append(JSON.toJSONString(create));
        	
        	if(jp.getExtParams()!=null){
        		jp.setBizId(jp.getExtParams().get("bizId"));
        		jp.setBizType(jp.getExtParams().get("bizType"));
        		jp.setEventType(jp.getExtParams().get("eventType"));
        	}
        	
        	builder.append(Delimiters);
        	builder.append(JSON.toJSONString(jp));
        	builder.append(Delimiters);
        }
        
        
        submitAsync(EsOpType.BULKSAVE.getValue(), builder.toString());
    
    }
    

    public void submitAsync(final String service, final String content) {
    	
    	if(!isShutDown.get()) {
    		
    		//异步处理+熔断控制
    		try {
				ThreadPoolFactory.getThreadPool().submit(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							
							submitSync(service, content);
							
						} catch (Exception e) {
							
							logger.error("任务["+content+"]==>"+service+"失败", e);
							
							if(failCount.getAndIncrement() > 50) {
								
								if(isShutDown.compareAndSet(false, true)) {
									
									failCntClearTask.cancel();
									
									//恢复重试
									timer.schedule(new TimerTask() {
										
										@Override
										public void run() {
											failCount.getAndSet(0);
											startFixedRateClean();
											isShutDown.compareAndSet(true, false);
										}
										
									}, 5*60*1000);
									
									logger.error("ES日志服务熔断");
									
									if(StringUtils.isNotEmpty(alarmEmail)) {

										SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
										EmailAlarmMessage message = new EmailAlarmMessage();
										message.setTitle("LTS-ES日志服务预警");
										message.setMsg("JobTracker-"+nodeAddress+"节点于"+dateFormat.format(new Date())+"发生ES日志服务熔断，请确认！");
										message.setTo(alarmEmail);
										
										alarmNotifier.notice(message);
									}

								}
								
							}
					
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("提交线程池写入任务日志失败", e);
			}
    		
    	}
    	
    	
    }
    
	public String submitSync(String service, String content) throws IOException {

		long start = System.currentTimeMillis();
		 
        StringEntity entity = new StringEntity(content, ContentType.APPLICATION_JSON);
		HttpPost httpPost = null;
		
		try {
			
			HttpClient httpClient = HttpClientFactory.getHttpClient();

			httpPost = new HttpPost(getLoadBalanceUrl()+"/"+service);
			httpPost.setEntity(entity);

			HttpResponse httpResponse = httpClient.execute(httpPost);
      
			int status = httpResponse.getStatusLine().getStatusCode();
			String result = EntityUtils.toString(httpResponse.getEntity());
			if(!Pattern.matches("2\\d\\d", String.valueOf(status))) {
				logger.error("任务["+content+"]==>"+service+"失败,"+"响应状态["+status+"],"+"响应内容["+result+"]");
				return null;
			}
			
			return result;
			
		} finally {
			if(httpPost!=null) {
				httpPost.releaseConnection();
			}
			
			logger.info(service+"日志耗时:"+(System.currentTimeMillis()-start));
		}
		
        
	}
	
	
	@Override
    public PaginationRsp<JobLogPo> search(JobLoggerRequest request) {
		
        PaginationRsp<JobLogPo> response = new PaginationRsp<JobLogPo>();
    
    	if(null!=request.getStartLogTime()){
    		request.setStartLogTimeMill(request.getStartLogTime().getTime());
    	}
    	
    	if(null!=request.getEndLogTime()){
    		request.setEndLogTimeMill(request.getEndLogTime().getTime());
    	}
    	try {
    		JSONArray must = new JSONArray();
    		Field[] fields = JobLoggerRequest.class.getDeclaredFields();
    		for(Field field : fields) {
    			field.setAccessible(true);
    			EsLogFilter filter = field.getAnnotation(EsLogFilter.class);
    			if(filter!=null) {
    				Object value = field.get(request);
    				if(!isEmpty(value)) {
    					if(Optype.Term.equals(filter.opType())) {
    						must.add(json("term", json(filter.name(), value)));
    					} else {
    						must.add(json("range", json(filter.name(), json(filter.extra(), value))));
    					}
    				}
    			}
    		}
    		
    		JSONObject search = new JSONObject();
    		search.put("from", request.getStart());
    		search.put("size", request.getLimit());
    		search.put("sort", json("logTime", json("order", "desc")));
    		
        	if(must.size() > 0) {
        		search.put("query", json("filtered", json("filter", json("bool", json("must", must)))));
        	} else {
        		search.put("query", json("match_all", new JSONObject()));
        	}
    		
        	
        	String result = submitSync(EsOpType.SEARCH.getValue(), JSON.toJSONString(search));
        	if(result!=null) {

        		List<JobLogPo> rows = new ArrayList<JobLogPo>();
        		
        		JSONObject hits = JSONObject.parseObject(result).getJSONObject("hits");
        		JSONArray hitDocs = hits.getJSONArray("hits");
        		
        		for(Object hitDoc : hitDocs) {
        			JobLogPo jobLogPo = JSON.parseObject(((JSONObject)hitDoc).getJSONObject("_source").toString(), JobLogPo.class);
        			rows.add(jobLogPo);
        		}
        		
        		response.setRows(rows);
        		response.setResults(hits.getIntValue("total"));
        		
        	}
        	
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("日志查询失败", e);
		}
    
		return response;
    
	}
	
    private String getLoadBalanceUrl() {
    	
    	String[] urls = ES_URL.split(",");
    	
    	Random random = new Random();
    	
    	return urls[random.nextInt(urls.length)];
    	
    }
    
	private JSONObject json(String key, Object value) {
		
		JSONObject json = new JSONObject();
		
		json.put(key, value);
		
		return json;
		
	}
	
	private boolean isEmpty(Object value) {
		
		if(value == null) {
			return true;
		}
		
		if(value instanceof Number) {
			return value.equals(0);
		}
		
		if(value instanceof String) {
			return StringUtils.isEmpty((String)value);
		}
		
		return false;
		
	}
	
	private void startFixedRateClean() {
		
		failCntClearTask = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				failCount.getAndSet(0);
			}
		};
	
		timer.scheduleAtFixedRate(failCntClearTask, 60*1000, 60*1000);

	}

}
