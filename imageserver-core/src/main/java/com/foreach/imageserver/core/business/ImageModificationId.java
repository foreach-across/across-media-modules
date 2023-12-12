package com.foreach.imageserver.core.business;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageModificationId implements Serializable
{
	@Column(name = "image_id")
	private long imageId;
	@Column(name = "context_id")
	private long contextId;
	@Column(name = "resolution_id")
	private long resolutionId;

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		ImageModificationId that = (ImageModificationId) o;
		return imageId == that.imageId &&
				contextId == that.contextId &&
				resolutionId == that.resolutionId;
	}

	@Override
	public int hashCode() {

		return Objects.hash( imageId, contextId, resolutionId );
	}

}
