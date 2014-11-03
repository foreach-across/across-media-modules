package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.Task;
import com.foreach.across.modules.taskrunner.business.TaskStatus;
import com.foreach.across.modules.taskrunner.business.TouchableTask;

import java.util.Date;

public class TouchableTaskWrapper<T> implements TouchableTask<T>
{
	private final TaskTransitionManager transitionManager;
	private final Task<T> original;

	public TouchableTaskWrapper(
			TaskTransitionManager transitionManager, Task<T> original ) {
		this.transitionManager = transitionManager;
		this.original = original;
	}

	@Override
	public void touch() {
		transitionManager.touch( original );
	}

	@Override
	public String getId() {
		return original.getId();
	}

	@Override
	public String getCreatedBy() {
		return original.getCreatedBy();
	}

	@Override
	public <T1 extends T> T1 getParameters() {
		return original.getParameters();
	}

	@Override
	public <R> R getResult() {
		return original.getResult();
	}

	@Override
	public TaskStatus getStatus() {
		return original.getStatus();
	}

	@Override
	public Date getCreated() {
		return original.getCreated();
	}

	@Override
	public Date getUpdated() {
		return original.getUpdated();
	}

	@Override
	public Date getExpiryDate() {
		return original.getExpiryDate();
	}
}
