package com.lvmama.tnt.lts.job.run;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import com.lvmama.tnt.lts.job.constant.JobParamEnum;
import com.lvmama.tnt.pushplatform.push.dto.PushMessage;
import com.lvmama.tnt.pushplatform.push.service.PushSendService;

public class PushJobRunner implements JobRunner {

	private PushSendService pushSendService;

	public Result run(JobContext jobContext) throws Throwable {
		Job job = jobContext.getJob();
		PushMessage pushMessage = convert(job);
		pushSendService.push(pushMessage);
		return new Result(Action.EXECUTE_SUCCESS);
	}

	private PushMessage convert(Job job) {
		String objectId = job.getParam(JobParamEnum.BIZID.name());
		String eventType = job.getParam(JobParamEnum.pushEventType.name());
		String objectType = job.getParam(JobParamEnum.objectType.name());

		PushMessage pushMessage = new PushMessage();
		pushMessage.setObjectId(objectId);
		pushMessage.setObjectType(objectType);
		pushMessage.setEventType(eventType);
		return pushMessage;
	}

	public void setPushSendService(PushSendService pushSendService) {
		this.pushSendService = pushSendService;
	}

}
