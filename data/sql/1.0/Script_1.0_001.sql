CREATE TABLE Application (
  id int IDENTITY(1,1) NOT NULL,
  name nvarchar(50) NOT NULL,
  callbackurl nvarchar(200) NULL,

  CONSTRAINT PK_application PRIMARY KEY CLUSTERED (id ASC)
)

GO