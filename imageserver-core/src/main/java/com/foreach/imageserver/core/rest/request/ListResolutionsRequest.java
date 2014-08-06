package com.foreach.imageserver.core.rest.request;

import java.util.Objects;

/**
 * @author Arne Vandamme
 */
public class ListResolutionsRequest
{
	private String context;
	private boolean configurableOnly;

	public String getContext() {
		return context;
	}

	public void setContext( String context ) {
		this.context = context;
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
				&& Objects.equals( context, that.context );
	}

	@Override
	public int hashCode() {
		return Objects.hash( context, configurableOnly );
	}
}
