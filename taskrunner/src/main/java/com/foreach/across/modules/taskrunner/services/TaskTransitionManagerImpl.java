package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.Task;
import com.foreach.across.modules.taskrunner.business.TaskResult;
import org.springframework.transaction.annotation.Transactional;

public class TaskTransitionManagerImpl implements TaskTransitionManager
{
	@Transactional
	@Override
	public void reschedule( Task task ) {

	}

	@Transactional
	@Override
	public void begin( Task task ) {

	}

	@Transactional
	@Override
	public void touch( Task task ) {

	}

	@Transactional
	@Override
	public void finish( Task task, TaskResult result ) {
		// get currently persisted

		// update properties

		// save
		if ( result.isFailed() ) {

		}
	}

	/*
	private void finishTask( ReportResult result ) {
		PersistedTaskDto dto = new PersistedTaskDto();
		BeanUtils.copyProperties( reportTask, dto );
		dto.setStatus( TaskStatus.FINISHED );
		dto.setResult( taskObjectsSerializer.serialize( result ) );
		dto.setUpdated( new Date() );
		reportTask = BasicServiceHelper.save( dto, PersistedTask.class, taskRepository );

		reportTaskResult.setReportTask( reportTask );
	}

	private void performStateChange( TaskStatus taskStatus ) {
		PersistedTaskDto dto = new PersistedTaskDto();
		BeanUtils.copyProperties( reportTask, dto );
		dto.setStatus( taskStatus );
		dto.setUpdated( new Date() );
		reportTask = BasicServiceHelper.save( dto, PersistedTask.class, taskRepository );
	}*/
}
