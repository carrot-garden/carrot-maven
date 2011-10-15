package com.carrotgarden.m2e.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenJob extends Job {

	private static final Logger log = LoggerFactory.getLogger(MavenJob.class);

	private static final AtomicInteger COUNT = new AtomicInteger(0);

	final MavenContext context;

	public MavenJob(final MavenContext context) {

		super(context.getKey());

		this.context = context;

		setPriority(Job.BUILD);

		// addJobChangeListener(listener);

	}

	private volatile Thread thread;

	@SuppressWarnings("unused")
	private final IJobChangeListener listener = new JobChangeAdapter() {
		@Override
		public void done(final IJobChangeEvent event) {

			final IStatus result = event.getResult();

			log.info("### result : {}", result);

		}
	};

	private final int count = COUNT.getAndIncrement();

	@Override
	protected IStatus run(final IProgressMonitor monitor) {

		monitor.beginTask("running maven goals", IProgressMonitor.UNKNOWN);

		log.info("### START :  {} : {}", count, context.getKey());

		context.execute(monitor);

		log.info("### FINISH : {} : {}", count, context.getKey());

		monitor.done();

		return Status.OK_STATUS;

	}

	// @Override
	protected IStatus runXXX(final IProgressMonitor monitor) {

		monitor.beginTask("running maven goals", IProgressMonitor.UNKNOWN);

		log.info("### START : {}", count);

		try {
			thread = Thread.currentThread();
			Thread.sleep(10 * 1000);
		} catch (final InterruptedException e) {
			log.info("### TRAP : {}", count);
		}

		log.info("### FINISH : {}", count);

		monitor.done();

		return Status.OK_STATUS;

	}

	@Override
	protected void canceling() {

		log.info("### CANCEL : {}", count);

		final Thread thread = this.thread;

		if (thread != null) {
			thread.interrupt();
		}

		context.cancel();

	}

}
