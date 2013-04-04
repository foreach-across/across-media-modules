package com.foreach.imageserver.dao.selectors;

public class ImageSelector extends AbstractSelector
{
	private String path, originalFileName, extension;
    private Integer applicationId;
    private Integer groupId;

    public final String getPath() {
        return path;
    }

    public final void setPath(String path) {
        this.path = path;
    }

    public final String getOriginalFileName() {
        return originalFileName;
    }

    public final void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public final String getExtension() {
        return extension;
    }

    public final void setExtension(String extension) {
        this.extension = extension;
    }

    public final Integer getApplicationId() {
        return applicationId;
    }

    public final void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public final Integer getGroupId() {
        return groupId;
    }

    public final void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public static final ImageSelector onNothing()
	{
		return new ImageSelector();
	}

	public static final ImageSelector onPathAndOriginalFileNameAndExtension( String path, String originalFileName, String extension)
	{
		ImageSelector selector = new ImageSelector();
		selector.setPath(path);
        selector.setOriginalFileName(originalFileName);
        selector.setExtension(extension);
		return selector;
	}

    public static final ImageSelector onApplicationId( Integer applicationId)
	{
		ImageSelector selector = new ImageSelector();
		selector.setApplicationId(applicationId);
		return selector;
	}

    public static final ImageSelector onGroupId(Integer groupId)
	{
		ImageSelector selector = new ImageSelector();
        selector.setGroupId(groupId);
		return selector;
	}

    public static ImageSelector onApplicationIdAndGroupId(int applicationId, int groupId) {
        ImageSelector selector = new ImageSelector();
        selector.setApplicationId(applicationId);
        selector.setGroupId(groupId);
		return selector;
    }

	@Override
	public final boolean equals( Object o )
	{
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageSelector other = (ImageSelector) o;

		if ( !nullSafeCompare( path, other.path ) ) {
			return false;
		}
        if ( !nullSafeCompare( originalFileName, other.originalFileName ) ) {
			return false;
		}
        if ( !nullSafeCompare( extension, other.extension ) ) {
			return false;
		}
        if ( !nullSafeCompare( applicationId, other.applicationId ) ) {
			return false;
		}
        if ( !nullSafeCompare( groupId, other.groupId ) ) {
			return false;
		}

		return true;
	}

	@Override
	@SuppressWarnings( "all" )
	public final int hashCode()
	{
		int result = ( path != null ) ? path.hashCode() : 0;
        result = addMultiplyHash(result, originalFileName);
        result = addMultiplyHash(result, extension);
        result = addMultiplyHash(result, applicationId);
        result = addMultiplyHash(result, groupId);
		return result;
	}
}
