CREATE TABLE Crop (
    id BIGINT IDENTITY(1,1) NOT NULL,
    imageid BIGINT NOT NULL,
    width INT NULL,
    height INT NULL,
    ratiowidth INT NULL,
    ratioheight INT NULL,
    version INT NULL,
    originX INT NULL,
    originY INT NULL,
    targetwidth INT NULL,

    CONSTRAINT PK_crop PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_crop_servableimage FOREIGN KEY (imageid) REFERENCES dbo.ServableImage (id),

    -- alternate primary key
    CONSTRAINT AK_crop UNIQUE(imageid,ratiowidth,ratioheight,targetwidth,version)
)

GO