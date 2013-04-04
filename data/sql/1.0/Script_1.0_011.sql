CREATE TABLE AppUser(
	id INT IDENTITY(1,1) NOT NULL,
	username NVARCHAR(50) NOT NULL,
	password NVARCHAR(50) NOT NULL,
	isadministrator BIT NOT NULL,
	isactive BIT NOT NULL,

    CONSTRAINT PK_appuser PRIMARY KEY ( id ASC)
)
GO