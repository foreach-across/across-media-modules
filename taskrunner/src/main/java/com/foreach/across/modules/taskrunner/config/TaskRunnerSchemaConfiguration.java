package com.foreach.across.modules.taskrunner.config;

import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.database.SchemaObject;

import java.util.Arrays;

public class TaskRunnerSchemaConfiguration extends SchemaConfiguration
{
	public static final String TABLE_TASK = "trm_task";

	public TaskRunnerSchemaConfiguration() {
		super( Arrays.asList( new SchemaObject( "table.task", TABLE_TASK ) ) );
	}
}
