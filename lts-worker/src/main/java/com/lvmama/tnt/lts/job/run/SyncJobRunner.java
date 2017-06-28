package com.lvmama.tnt.lts.job.run;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import com.lvmama.tnt.pushplatform.push.dto.SyncMessage;
import com.lvmama.tnt.pushplatform.push.service.SyncService;

public class SyncJobRunner implements JobRunner {

    private SyncService syncService;

    public Result run(JobContext jobContext) throws Throwable {
        SyncMessage message = convert(jobContext);
        syncService.sync(message);
        return new Result(Action.EXECUTE_SUCCESS);
    }

    private SyncMessage convert(JobContext jobContext) {
        SyncMessage target = new SyncMessage();
        return target;
    }

}
