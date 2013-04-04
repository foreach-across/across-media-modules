package com.foreach.imageserver.business.image;

import com.foreach.imageserver.business.math.Category;

import java.util.*;

/**
 * ServableImage object extended with the crops for the image.
 */
public class ServableImageData extends ServableImage
{
	private Set<Crop> crops = new HashSet<Crop>();

	public final Set<Crop> getCrops()
	{
		return crops;
	}

	public final void setCrops( Set<Crop> crops )
	{
		this.crops = crops;
	}

    public final List<Integer> getVersionList()
    {
        List<Integer> versions = new ArrayList<Integer>();
        Category.project( crops, versions, new Crop.VersionFunction() );
        return versions;
    }
}
