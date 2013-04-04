--Change datatype of id column in Crop table to BIGINT

--Drop PK_Constraint
ALTER TABLE Crop DROP CONSTRAINT PK_crop

GO
--Alter id_column
ALTER TABLE Crop ALTER COLUMN id BIGINT

GO
--Create PK_Constraint
ALTER TABLE Crop ADD CONSTRAINT PK_crop PRIMARY KEY CLUSTERED (id ASC)

GO