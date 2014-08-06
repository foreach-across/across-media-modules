package com.foreach.imageserver.core.rest.request;

import java.util.Objects;

/**
 * @author Arne Vandamme
 */
public class ListResolutionsRequest
{
	private String contextCode;
	private boolean configurableOnly;

	public String getContextCode() {
		return contextCode;
	}

	public void setContextCode( String contextCode ) {
		this.contextCode = contextCode;
	}

	public boolean isConfigurableOnly() {
		return configurableOnly;
	}

	public void setConfigurableOnly( boolean configurableOnly ) {
		this.configurableOnly = configurableOnly;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ListResolutionsRequest that = (ListResolutionsRequest) o;

		return Objects.equals( configurableOnly, that.configurableOnly )
				&& Objects.equals( contextCode, that.contextCode );
	}

	@Override
	public int hashCode() {
		return Objects.hash( contextCode, configurableOnly );
	}
}
