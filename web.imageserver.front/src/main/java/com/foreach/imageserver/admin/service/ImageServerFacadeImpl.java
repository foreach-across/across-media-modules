package com.foreach.imageserver.admin.service;

import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.services.GroupService;
import com.foreach.imageserver.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageServerFacadeImpl implements ImageServerFacade{

	@Autowired
	private ImageService imageService;

	@Autowired
	private GroupService groupService;

    public final ServableImageData getImageData(long imageId) {
        return imageService.getImageById( imageId );
    }

    public final Group getImageGroup(int groupId) {
        return groupService.getGroupById( groupId );
    }
}
