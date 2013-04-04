package com.foreach.imageserver.api.business;

/**
 * An enum that defines all the possible states of an upload to the image server.
 */
public enum UploadStatus
{
    /**
     * represents a successful image upload
     */
	SUCCES                  ( 1, "image uploaded", false ),
    /**
     * represents an authentication error
     */
	AUTHENTICATION_ERROR    ( 2, "could not authenticate", true ),
    /**
     * represents a content error
     */
	CONTENT_ERROR           ( 3, "content error", true ),
    /**
     * represents a server side error
     */
	SERVER_ERROR            ( 4, "application error, serverside", true ),
    /**
     * represents an image not found error
     */
	IMAGE_NOT_FOUND_ERROR   ( 5, "unknown image id", true ),

	/**
     * represents an response parsing error
     */
	PARSE_ERROR             ( 1001, "server reponse unintelligible", true ),
    /**
     * represents a client side error
     */
	CLIENT_ERROR            ( 1002, "application error, clientside", true );

	private int id;
	private String description;
	private boolean failure;

    /**
     * The default constructor
     *
     * @param id the id of the upload status
     * @param description a description for the upload status
     * @param failure true if the upload succeeded, false otherwise
     */
	private UploadStatus( int id, String description, boolean failure) {
	    this.id = id;
		this.description = description;
	    this.failure = failure;
	}

    /**
     *
     * @return the id of the <code>UploadStatus</code> enum object
     */
	public final int getId()
	{
		return id;
	}

    /**
     *
     * @return the description of the <code>UploadStatus</code> enum object
     */
	public final String getDescription()
	{
	    return description;
	}

    /**
     *
     * @return true if this status represents a failure
     */
	public final boolean isFailure()
	{
	    return failure;
	}

    /**
     * Returns a code>UploadStatus</code> enum object that matches the given id.
     *
     * @param
     * @return a <code>UploadStatus</code> enum object
     */
	public static final UploadStatus getById( int id )
	{
		for ( UploadStatus s : values() ) {
			if ( s.getId() == id ) {
				return s;
			}
		}

		return null;
	}
}
