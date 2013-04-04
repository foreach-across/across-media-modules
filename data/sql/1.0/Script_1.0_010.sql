CREATE TABLE AuthUser_Group(
	userid BIGINT NOT NULL,
	groupid INT NOT NULL,

    CONSTRAINT PK_usergroup PRIMARY KEY ( userid ASC, groupid ASC),

    CONSTRAINT FK_usergroup_user FOREIGN KEY (userid) REFERENCES dbo.AuthUser (id),
    CONSTRAINT FK_usergroup_group FOREIGN KEY (groupid) REFERENCES dbo.ImageGroup (id)
)
GO