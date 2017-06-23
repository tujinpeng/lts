package com.lvmama.tnt.lts.job.run;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import com.lvmama.tnt.pushplatform.push.dto.UpdateMessage;
import com.lvmama.tnt.pushplatform.push.service.UpdateService;

public class UpdateJobRunner implements JobRunner {

	private UpdateService updateService;

	public Result run(JobContext jobContext) throws Throwable {
		UpdateMessage message = convert(jobContext);
		updateService.update(message);
		return new Result(Action.EXECUTE_SUCCESS);
	}

	private UpdateMessage convert(JobContext jobContext) {
		UpdateMessage target = new UpdateMessage();
		return target;
	}

}
