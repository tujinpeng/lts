package com.github.ltsopensource.spring.tasktracker;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by chenwenshun on 16/7/7.
 * taskTracker 任务分发
 */
public class JobDispatcherSpringBean implements JobRunner,ApplicationContextAware {

    private static  ApplicationContext _applicationContext;

    private String shardField = "processor";

    public void setShardField(String shardField) {
        this.shardField = shardField;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
           _applicationContext= applicationContext;
    }

    @Override

    public Result run(JobContext jobContext) throws Throwable {
        Job job = jobContext.getJob();

        String value;
//        if (shardField.equals("taskId")) {
//            value = job.getTaskId();
//        } else {
//            value = job.getParam(shardField);
//        }
        value = job.getParam(shardField);

        JobRunner jobRunner = null;
        if (StringUtils.isNotEmpty(value)) {
            //spring Context 中取执行类
            jobRunner = (JobRunner)_applicationContext.getBean(value);
        }
        if (jobRunner == null) {
            jobRunner = JobRunnerHolder.getJobRunner("_LTS_DEFAULT");

            if (jobRunner == null) {
                throw new JobDispatchException("Can not find JobRunner by Shard Value : [" + value + "]");
            }
        }
        return jobRunner.run(jobContext);
    }
}
