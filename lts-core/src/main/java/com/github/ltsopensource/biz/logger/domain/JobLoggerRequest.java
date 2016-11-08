package com.github.ltsopensource.biz.logger.domain;

import com.github.ltsopensource.admin.request.PaginationReq;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 6/11/15.
 */
public class JobLoggerRequest extends PaginationReq {

    private String realTaskId;
    private String taskId;

    private String taskTrackerNodeGroup;
    
    
    private String bizId;
    
    private String eventType;

    private Date startLogTime;

    private Date endLogTime;

    private String submitNodeGroup;//提交节点组
    
    private String logType; // 日志类型
    
    private String jobType;//类型
    
    private String success;//执行结果
    
    public String getRealTaskId() {
        return realTaskId;
    }

    public void setRealTaskId(String realTaskId) {
        this.realTaskId = realTaskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }
    
	public Date getStartLogTime() {
        return startLogTime;
    }

    public void setStartLogTime(Date startLogTime) {
        this.startLogTime = startLogTime;
    }

    public Date getEndLogTime() {
        return endLogTime;
    }

    public void setEndLogTime(Date endLogTime) {
        this.endLogTime = endLogTime;
    }

	public String getBizId() {
		return bizId;
	}

	public void setBizId(String bizId) {
		this.bizId = bizId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
    
	 public String getSubmitNodeGroup() {
		return submitNodeGroup;
	}

	public void setSubmitNodeGroup(String submitNodeGroup) {
		this.submitNodeGroup = submitNodeGroup;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

    
}
