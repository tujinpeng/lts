package com.lvmama.tnt.lts.job.run;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import com.lvmama.tnt.pushplatform.dto.PushMessage;
import com.lvmama.tnt.pushplatform.service.PushService;

public class PushJobRunner implements JobRunner {

	private PushService pushService;

	public Result run(JobContext jobContext) throws Throwable {
		PushMessage pushMessage = convert(jobContext);
		pushService.push(pushMessage);
		return new Result(Action.EXECUTE_SUCCESS);
	}

	private PushMessage convert(JobContext jobContext) {
		PushMessage pushTarget = new PushMessage();
		return pushTarget;
	}

}
