package com.foreach.across.modules.taskrunner.business;

import java.util.Date;

/**
 * @author Arne Vandamme
 */
public interface MutableTask<P> extends TouchableTask<P>
{
	void setCreatedBy( String createdBy );

	void setParameters( P parameters );

	void setResult( Object result );

	void setStatus( TaskStatus status );

	void setCreated( Date created );

	void setUpdated( Date updated );

	void setExpiryDate( Date expiryDate );
}
