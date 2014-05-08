package com.foreach.imageserver.core.business;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * TODO Re-document this after I'm through refactoring.
 */
public class Image {
    private int id;
    private Date dateCreated;
    private String repositoryCode;
    private Dimensions dimensions;
    private ImageType imageType;

    private String dateCreatedYearString;
    private String dateCreatedMonthString;
    private String dateCreatedDayString;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;

        if (dateCreated != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateCreated);

            dateCreatedYearString = Integer.toString(cal.get(Calendar.YEAR));
            dateCreatedMonthString = StringUtils.leftPad(Integer.toString(cal.get(Calendar.MONTH) + 1), 2, '0');
            dateCreatedDayString = StringUtils.leftPad(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2, '0');
        } else {
            dateCreatedYearString = null;
            dateCreatedMonthString = null;
            dateCreatedDayString = null;
        }
    }

    public String getRepositoryCode() {
        return repositoryCode;
    }

    public void setRepositoryCode(String repositoryCode) {
        this.repositoryCode = repositoryCode;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public String getDateCreatedYearString() {
        return dateCreatedYearString;
    }

    public String getDateCreatedMonthString() {
        return dateCreatedMonthString;
    }

    public String getDateCreatedDayString() {
        return dateCreatedDayString;
    }
}