
INSERT INTO Application ( name  ) VALUES ( 'DesktopImagesServer' );

INSERT INTO ImageGroup ( name, applicationid ) VALUES ( 'Public', 1 );

INSERT INTO ImageGroup ( name, applicationid ) VALUES ( 'InHouse', 1 );


-- insert formats for the defaults: the admin thumbnail and the croopping tool preview

INSERT INTO Format ( name, groupid, width, height, ratiowidth, ratioheight )
VALUES ( 'preview', 1, 64, 64, 1, 1 );

INSERT INTO Format ( name, groupid, width, height, ratiowidth, ratioheight )
VALUES ( 'preview', 2, 64, 64, 1, 1 );

INSERT INTO Format ( name, groupid, width, height, ratiowidth, ratioheight )
VALUES ( 'cropping-tool', 1, 400, 0, 0, 0 );

INSERT INTO Format ( name, groupid, width, height, ratiowidth, ratioheight )
VALUES ( 'cropping-tool', 2, 400, 0, 0, 0 );


insert into AppUser( username, password, isadministrator, isactive )
values ( 'admin', 'admin', 1, 1);

insert into AppUser( username, password, isadministrator, isactive )
values ( 'client', 'client', 0, 1);

insert into AuthUser( userkey ) values ('admin');

insert into AuthUser( userkey ) values ('apiTest');

insert into AuthUser_Group( userid, groupid ) values ( 1, 1);
insert into AuthUser_Group( userid, groupid ) values ( 1, 2);
insert into AuthUser_Group( userid, groupid ) values ( 2, 2);