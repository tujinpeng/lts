package com.github.ltsopensource.biz.logger.es;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.biz.logger.JobLogger;
import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.JobLoggerRequest;
import com.github.ltsopensource.biz.logger.es.util.EsFailCntClearTimer;
import com.github.ltsopensource.biz.logger.es.util.HttpClientFactory;
import com.github.ltsopensource.biz.logger.es.util.ThreadPoolFactory;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

public class EsJobLogger implements JobLogger
{
	
	private Logger logger = LoggerFactory.getLogger(EsJobLogger.class);
	
	private static String ES_URL;
	
	private AtomicInteger failCount = new AtomicInteger(0);
	
	private volatile boolean isShutDown;
	
	private Timer timer;
	
	public EsJobLogger(Config config) {
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new EsFailCntClearTimer(failCount), 1000, 60*1000);
		
		ES_URL = config.getParameter("es.log.url");
		
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
        
        submitAsync("saveOne", toJsonString(jobLogPo));
    
	}
    
    @Override
    public void log(List<JobLogPo> jobLogPos) {
        
    	if (CollectionUtils.isEmpty(jobLogPos)) {
            return;
        }
    	
        for(JobLogPo jp: jobLogPos){
        	if(jp.getExtParams()!=null){
        		jp.setBizId(jp.getExtParams().get("bizId"));
        		jp.setBizType(jp.getExtParams().get("bizType"));
        		jp.setEventType(jp.getExtParams().get("eventType"));
        	}
        }
        
        submitAsync("saveAll", toJsonString(jobLogPos));
    
    }
    

    public void submitAsync(final String service, final String content) {
    	
    	if(!isShutDown) {
    		
    		//异步处理+熔断控制
    		try {
				ThreadPoolFactory.getThreadPool().submit(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							
							submitSync(service, content);
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							logger.error("任务["+content+"]==>"+service+"失败", e);
							
							if(failCount.getAndIncrement() > 30) {
								
								isShutDown = true;
								
								timer.cancel();
								
								logger.error("日志写入ES服务关闭");
							
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
		
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "java.lang.String");
        map.put("value", URLEncoder.encode(content, "utf-8"));
        
        List<Map<String, Object>> args = Collections.singletonList(map);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("isPassedBy", "true"));
        params.add(new BasicNameValuePair("arguments", toJsonString(args)));

		HttpPost httpPost = null;
		
		try {
			
			HttpClient httpClient = HttpClientFactory.getHttpClient();
			httpPost = new HttpPost(ES_URL+"/"+service);
			
			httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse httpResponse = httpClient.execute(httpPost);
      
			int status = httpResponse.getStatusLine().getStatusCode();
			String result = EntityUtils.toString(httpResponse.getEntity());
			if(status != 200) {
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
        
    	//eg:第一页(0,10)传送0，第二页(10,10)传送1....
    	Integer pageNum = request.getStart();
    	if(pageNum!=0){
    		pageNum = request.getStart()/request.getLimit();
    	}
    	
    	if(null!=request.getStartLogTime()){
    		request.setStartLogTimeMill(request.getStartLogTime().getTime());
    	}
    	
    	if(null!=request.getEndLogTime()){
    		request.setEndLogTimeMill(request.getEndLogTime().getTime());
    	}
    	
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("pageSize",request.getLimit());
    	map.put("pageNum",pageNum);
    	map.put("parameter",request);

    	try {
    		
			String count = submitSync("count", toJsonString(map));
			
			if(StringUtils.isEmpty(count) || count.equals("0")) {	
				response.setRows(Collections.<JobLogPo> emptyList());
				return response;
			}
			
			String searchResult = submitSync("query", toJsonString(map));
			
			List<JobLogPo> rows = new ArrayList<JobLogPo>();
			JSONArray jobLogPoList= JSONArray.fromObject(searchResult);
			
			//返回的extParams不是标准json对象格式
			if (jobLogPoList!=null && !jobLogPoList.isEmpty()) {
				
				for (Object o : jobLogPoList) {
					JobLogPo po = (JobLogPo)JSONObject.toBean((JSONObject)o, JobLogPo.class);
					rows.add(po);
				}
				
			}
			
			response.setResults(Long.valueOf(count).intValue());
			response.setRows(rows);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("查询第"+pageNum+"页日志失败", e);
		}
    
		return response;
    
	}
	
	public <T> String toJsonString(T object) {
		
		String result = null;
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			result = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		
		return result;
	}

}
