package com.lvmama.tnt.lts.job.run;

import java.util.ArrayList;
import java.util.List;

import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

public class CompositeJobRunner implements JobRunner {

	private final List<JobRunner> jobRunners;

	public CompositeJobRunner() {
		jobRunners = new ArrayList<JobRunner>();
	}

	public Result run(JobContext jobContext) throws Throwable {
		if (jobRunners != null && !jobRunners.isEmpty()) {
			for (JobRunner runner : jobRunners) {
				runner.run(jobContext);
			}
		}
		return null;
	}

	public void addJobRunner(JobRunner jobRunner) {
		jobRunners.add(jobRunner);
	}

}
