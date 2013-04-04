CREATE TABLE Variant (
  id BIGINT IDENTITY(1,1) NOT NULL,
  imageid BIGINT NOT NULL,
  formatid INT NOT NULL,
  cropid BIGINT NULL,
  width INT NOT NULL,
  height INT NOT NULL,
  version INT NOT NULL,
  dateLastCalled datetime NOT NULL,
  dateCreated datetime NOT NULL DEFAULT GETDATE(),

  CONSTRAINT PK_variant PRIMARY KEY CLUSTERED (id ASC),
  CONSTRAINT FK_variant_image FOREIGN KEY (imageid) REFERENCES dbo.ServableImage (id),
  CONSTRAINT FK_variant_format FOREIGN KEY (formatid) REFERENCES dbo.Format (id),
  CONSTRAINT FK_variant_crop FOREIGN KEY (cropid) REFERENCES dbo.Crop (id),
  
  -- alternate primary key
  CONSTRAINT AK_variant UNIQUE(imageid,width,height,version)
)

GO