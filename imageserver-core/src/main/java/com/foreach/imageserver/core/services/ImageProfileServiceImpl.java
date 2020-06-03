package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageProfile;
import com.foreach.imageserver.core.repositories.ImageProfileRepository;
import com.foreach.imageserver.dto.ImageProfileDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ImageProfileServiceImpl implements ImageProfileService
{
	@Autowired
	private ImageProfileRepository imageProfileRepository;

	@Override
	public ImageProfile getDefaultProfile() {
		return imageProfileRepository.findAll().iterator().next();
	}

	@Override
	public Optional<ImageProfile> getById( long id ) {
		return imageProfileRepository.findById( id );
	}

	@Transactional
	@Override
	public void save( ImageProfileDto imageProfileDto ) {
		ImageProfile imageProfile;

		if ( imageProfileDto.isNewEntity() ) {
			imageProfile = new ImageProfile();
		}
		else {
			imageProfile = getById( imageProfileDto.getId() ).get();
		}

		BeanUtils.copyProperties( imageProfileDto, imageProfile );

		imageProfileRepository.save( imageProfile );

		BeanUtils.copyProperties( imageProfile, imageProfileDto );
	}
}
