package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.tasks.TaskParametersHashGenerator;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Default implementation of {@link com.foreach.across.modules.taskrunner.tasks.TaskParametersHashGenerator}
 * creating a hash based on the entire parameters object (taking all different properties into account).
 *
 * @author Arne Vandamme
 */
public class DefaultTaskParametersHashGenerator implements TaskParametersHashGenerator<Object>
{
	private final TaskObjectsSerializer serializer;

	public DefaultTaskParametersHashGenerator( TaskObjectsSerializer serializer ) {
		this.serializer = serializer;
	}

	@Override
	public String generateParametersHash( Object parameters ) {
		return DigestUtils.sha256Hex( serializer.serialize( parameters ) );
	}
}
