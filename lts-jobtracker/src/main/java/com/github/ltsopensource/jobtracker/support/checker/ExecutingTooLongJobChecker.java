package com.github.ltsopensource.jobtracker.support.checker;

import com.github.ltsopensource.alarm.AlarmMessage;
import com.github.ltsopensource.alarm.AlarmNotifier;
import com.github.ltsopensource.alarm.AlarmType;
import com.github.ltsopensource.alarm.email.EmailAlarmMessage;
import com.github.ltsopensource.alarm.email.EmailAlarmNotifier;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.queue.domain.JobPo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by chenwenshun on 2017/1/10.
 * 守护线程消息阻塞检查
 * 每隔5分钟检查执行中的任务执行时间超过20秒的发送报警提醒
 */
public class ExecutingTooLongJobChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutingTooLongJobChecker.class);

    private AlarmNotifier<EmailAlarmMessage> emailAlarmNotifier;

    private final ScheduledExecutorService tooLongJobChecker = Executors.newScheduledThreadPool(1,new NamedThreadFactory("LTS-ExecutingTooLongJob-Checker",true));
    private JobTrackerAppContext appContext;

    private ScheduledFuture<?> scheduledFuture;

    private final LinkedList<JobPo> jobPoLinkedList = new LinkedList<JobPo>();
    //     key jobId    value gmtModified
    private Map<String,Long> jobs = new ConcurrentHashMap<String, Long>();

    private final ReentrantLock lock = new ReentrantLock();



    private boolean offer(JobPo jobPo){
        lock.lock();
        try {
            if(jobs.containsKey(jobPo.getJobId())){
                Long gmtModified = jobs.get(jobPo.getJobId());
                if(gmtModified!=null && gmtModified.equals(jobPo.getGmtCreated())){
                    return false;
                }
            }

            if(jobPoLinkedList.size()>= 50){
                jobPoLinkedList.poll();
            }
            jobPoLinkedList.offer(jobPo);
            jobs.put(jobPo.getJobId(),jobPo.getGmtCreated());
            return true;

        }finally {
        lock.unlock();
        }
    }


    public ExecutingTooLongJobChecker(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        emailAlarmNotifier = new EmailAlarmNotifier(appContext);
    }

    private AtomicBoolean start = new AtomicBoolean(false);

    public void start(){
        try {
            if(start.compareAndSet(false,true)){
                scheduledFuture = tooLongJobChecker.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try{

                            LOGGER.info("Executing too Long job checker executing...........");
                                if(!appContext.getRegistryStatMonitor().isAvailable()){
                                    return;
                                }
                                //执行时间超过20秒的任务
                                List<JobPo> tooLongJobs = appContext.getExecutingJobQueue().getDeadJobs(
                                        SystemClock.now() - 20*1000);//20秒
                                LOGGER.info("too Long Job count:"+tooLongJobs.size());
                                String alarmEmail = appContext.getConfig().getParameter(ExtConfig.ALARM_EMAIL_TO);
                                if(StringUtils.isEmpty(alarmEmail))
                                    return;
                                String[] alarmEamils = alarmEmail.split(",");
                                LOGGER.info("too long alarm email:"+alarmEmail);

                                if(CollectionUtils.isNotEmpty(tooLongJobs)){
                                    StringBuilder msg;
                                    for (JobPo job:tooLongJobs) {
                                        if(offer(job)){
                                            msg = new StringBuilder();
                                            msg.append("taskId:"+job.getTaskId());
                                            msg.append(System.getProperty("line.separator"));
                                            msg.append("taskTrackerNodeGroup:"+job.getTaskTrackerNodeGroup());
                                            msg.append(System.getProperty("line.separator"));
                                            msg.append("extParams:"+job.getExtParams());
                                            msg.append(System.getProperty("line.separator"));
                                            msg.append("=========================华丽的分割线=====================");
                                            msg.append(System.getProperty("line.separator"));
                                            msg.append(job.toString());
                                            EmailAlarmMessage alarmMessage = new EmailAlarmMessage();
                                            alarmMessage.setTitle("LTS 阻塞消息报警！");
                                            alarmMessage.setMsg(msg.toString());
                                            alarmMessage.setTime(SystemClock.now());
                                            alarmMessage.setType(AlarmType.BLOCK);
                                            for (String to:alarmEamils) {
                                                alarmMessage.setTo(to);
                                                LOGGER.info("too long alarm mail:"+to+msg.toString());
                                                emailAlarmNotifier.notice(alarmMessage);

                                            }

                                        }


                                    }
                                }
                        }catch (Throwable t){
                            LOGGER.error("Executing too Long job checker exec failed ",t);
                        }

                    }
                },5,5, TimeUnit.MINUTES);
            }
            LOGGER.info("Executing too Long job checker started!");
        }catch (Throwable e){
            LOGGER.error("Executing too Long job checker start failed!",e);
        }

    }

    public void stop(){
        try {
            if(start.compareAndSet(true,false)){
                scheduledFuture.cancel(true);
                tooLongJobChecker.shutdown();
            }
        }catch (Throwable t){
            LOGGER.error("");
        }
    }
}
