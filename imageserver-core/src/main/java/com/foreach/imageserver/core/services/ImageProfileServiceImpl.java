package com.foreach.imageserver.core.services;

import com.foreach.across.modules.hibernate.util.BasicServiceHelper;
import com.foreach.imageserver.core.business.ImageProfile;
import com.foreach.imageserver.core.repositories.ImageProfileRepository;
import com.foreach.imageserver.dto.ImageProfileDto;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageProfileServiceImpl implements ImageProfileService
{
	@Autowired
	private ImageProfileRepository imageProfileRepository;

	@Override
	public ImageProfile getDefaultProfile() {
		return imageProfileRepository.getAll().iterator().next();
	}

	@Override
	public ImageProfile getById( long id ) {
		return imageProfileRepository.getById( id );
	}

	@Transactional
	@Override
	public void save( ImageProfileDto imageProfileDto ) {
		ImageProfile imageProfile;

		if ( imageProfileDto.isNewEntity() ) {
			imageProfile = new ImageProfile();
		}
		else {
			imageProfile = getById( imageProfileDto.getId() );
		}

		BeanUtils.copyProperties( imageProfileDto, imageProfile );

		if ( imageProfileDto.isNewEntity() ) {
			imageProfileRepository.create( imageProfile );
		}
		else {
			imageProfileRepository.update( imageProfile );
		}

		BeanUtils.copyProperties( imageProfile, imageProfileDto );
	}
}
