package com.carrotgarden.m2e.config;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class MavenJob extends Job {

	final MavenContext context;

	public MavenJob(final MavenContext context) {
		super(context.getName());
		this.context = context;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {

		context.execute(monitor);

		return Status.OK_STATUS;

	}

}
