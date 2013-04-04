CREATE TABLE ImageGroup (
  id int IDENTITY(1,1) NOT NULL,
  name nvarchar(50) NOT NULL,
  applicationid int not null,

  CONSTRAINT PK_group PRIMARY KEY CLUSTERED (id ASC),
  CONSTRAINT FK_group_application FOREIGN KEY (applicationid) REFERENCES dbo.Application (id),

  -- alternate primary key
  CONSTRAINT AK_group UNIQUE(applicationid,name)
)

GO