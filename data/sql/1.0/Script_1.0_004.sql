CREATE TABLE Format (
    id INT IDENTITY(1,1) NOT NULL,
    groupid INT NOT NULL,
    width INT NULL,
    height INT NULL,
    ratiowidth INT NULL,
    ratioheight INT NULL,
    name NVARCHAR(50),

    CONSTRAINT PK_format PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_format_group FOREIGN KEY (groupid) REFERENCES dbo.ImageGroup (id),

    -- alternate primary key
    CONSTRAINT AK_format UNIQUE(groupid,width,height,ratiowidth,ratioheight)

)

GO