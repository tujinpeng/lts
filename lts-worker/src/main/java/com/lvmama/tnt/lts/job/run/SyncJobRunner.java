package com.lvmama.tnt.lts.job.run;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import com.lvmama.tnt.lts.job.constant.JobParamEnum;
import com.lvmama.tnt.pushplatform.push.dto.SyncMessage;
import com.lvmama.tnt.pushplatform.push.service.SyncService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SyncJobRunner implements JobRunner,InitializingBean,ApplicationContextAware {

    private ApplicationContext applicationContext;
    private SyncService syncService;

    public Result run(JobContext jobContext) throws Throwable {
        SyncMessage message = convert(jobContext);
        syncService.sync(message);
        return new Result(Action.EXECUTE_SUCCESS);
    }

    private SyncMessage convert(JobContext jobContext) {
        SyncMessage target = new SyncMessage();
        Job job = jobContext.getJob();
        String objectId = job.getParam(JobParamEnum.BIZID.name());
        String eventType = job.getParam(JobParamEnum.pushEventType.name());
        String objectType = job.getParam(JobParamEnum.objectType.name());

        target.setObjectId(objectId);
        target.setEventType(eventType);
        target.setObjectType(objectType);
        return target;
    }

    public void setSyncService(SyncService syncService) {
        this.syncService = syncService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (syncService == null) {
            this.syncService = (SyncService) applicationContext.getBean("syncService");
        }
    }
}
