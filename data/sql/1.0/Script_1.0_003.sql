CREATE TABLE ServableImage (
  id BIGINT IDENTITY(1,1) NOT NULL,
  applicationid INT NOT NULL,
  groupid INT NOT NULL,
  width INT NULL,
  height INT NULL,
  filesize BIGINT NULL,
  filepath nvarchar(200) NULL,
  originalfilename nvarchar(50) NULL,
  extension nvarchar(50) NULL,
  datecreated datetime NOT NULL DEFAULT GETDATE(),
  deleted BIT NULL DEFAULT 0,

  CONSTRAINT PK_image PRIMARY KEY CLUSTERED (id ASC),
  CONSTRAINT FK_image_application FOREIGN KEY (applicationid) REFERENCES dbo.Application (id),
  CONSTRAINT FK_image_group FOREIGN KEY (groupid) REFERENCES dbo.ImageGroup (id)
)

GO