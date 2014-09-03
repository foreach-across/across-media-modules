package com.foreach.imageserver.core.rest.response;

import com.foreach.imageserver.core.rest.request.ListModificationsRequest;
import com.foreach.imageserver.dto.ImageModificationDto;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @author Arne Vandamme
 */
public class ListModificationsResponse extends ImageResponse
{
	private List<ImageModificationDto> modifications;

	public ListModificationsResponse() {
	}

	public ListModificationsResponse( ListModificationsRequest request ) {
		BeanUtils.copyProperties( request, this );
	}

	public void setModifications( List<ImageModificationDto> modifications ) {
		this.modifications = modifications;
	}

	public List<ImageModificationDto> getModifications() {
		return modifications;
	}
}
