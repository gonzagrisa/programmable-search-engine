-- use users
use buscador

drop table dbo.users

-- ################## TABLA USUARIOS ##################
CREATE TABLE dbo.users (
	user_id		INT IDENTITY(1,1)	NOT NULL,
	username	VARCHAR(50)			NOT NULL,
	name		VARCHAR(50)			NOT NULL,
	last_name	VARCHAR(50)			NOT NULL,
	password	VARBINARY(32)		NOT NULL,
	role		VARCHAR(20)			NOT NULL,
	status		BIT					NOT NULL,
	token_api	UNIQUEIDENTIFIER	default NEWID(),
	constraint PK__users__END primary key (user_id),
	constraint CK__users_role__END check (role in ('ADMIN', 'USER'))
);
go


/*
* *****************************************
*	PROCEDIMIENTOS ALMACENADOS
* *****************************************
*/

-------------------------- PROCEDIMIENTO ALMACENADO OBTENER TODOS LOS USUARIOS ACTIVOS --------------------------
CREATE OR ALTER PROCEDURE dbo.get_users
AS
BEGIN
	SELECT * FROM dbo.users u
		WHERE u.role = 'USER'
		and u.status = 1
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO CHEQUEAR SI NOMBRE DE USUARIO ESTA EN USO --------------------------
CREATE OR ALTER PROCEDURE dbo.check_username
(
	@username	varchar(50),
	@user_id	INT = -1
)
AS
BEGIN
	SELECT * from dbo.users u
		WHERE u.username = @username
		AND u.user_id != @user_id
		AND u.status = 1
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO OBTENER DATOS DE UN USUARIO --------------------------
CREATE OR ALTER PROCEDURE dbo.get_user_info
(
	@user_id	INT
)
AS
BEGIN
	SELECT * from dbo.users
		WHERE user_id = @user_id
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO LOGIN --------------------------
CREATE OR ALTER PROCEDURE dbo.validate_user
(
	@username	varchar(50),
	@password	varchar(50)
)
AS
BEGIN	
	declare @crypt varbinary(32)
	select @crypt = HASHBYTES('sha1', @password + replicate('*', 32 - len(@password)))

	select * from dbo.users u
		where u.username = @username
		and   u.password = @crypt
		and status = 1
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO REGISTRAR --------------------------
CREATE OR ALTER PROCEDURE dbo.new_user
(
	@username	varchar(50),
	@name		varchar(50),
	@last_name	varchar(50),
	@password	varchar(50),
	@role		varchar(20) = 'USER'
)
AS
BEGIN
	IF EXISTS (
		SELECT 1
		FROM dbo.users
		WHERE username = @username
			AND status = 1
	)
	BEGIN
		raiserror ('El nombre de usuario ya se encuentra en uso',16,1)
		return
	END
	declare @crypt varbinary(32)
	select @crypt = HASHBYTES('sha1', @password + replicate('*', 32 - len(@password)))

	insert into dbo.users (name, last_name, username, password, role, token_api, status)
	values	(@name, @last_name, @username, @crypt, @role, NEWID(), 1)	
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR INFORMACION USUARIO --------------------------
CREATE or ALTER PROCEDURE dbo.update_user
    @id			INT,
	@username	VARCHAR(50) = null,
	@name		VARCHAR(50) = null,
    @last_name	VARCHAR(50) = null,
	@password	VARCHAR(50) = null
AS
BEGIN
	IF (@password IS NOT null)
	BEGIN
		declare @crypt varbinary(32)
		select @crypt = HASHBYTES('sha1', @password + replicate('*', 32 - len(@password)))

		update u
		set password = @crypt
		from dbo.users u
		where u.user_id = @id
	END
	IF (@name IS NOT null AND @last_name IS NOT null AND @username IS NOT null)
	BEGIN
		update u
			set name = @name,
				last_name = @last_name,
				username = @username
			from dbo.users u
			where u.user_id = @id
	END
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO CHEQUEAR CONTRASE�A ANTIG�A --------------------------
CREATE or ALTER PROCEDURE dbo.check_password
    @id			INT,
	@password	VARCHAR(50)
AS
BEGIN
	declare @crypt varbinary(32)
	select @crypt = HASHBYTES('sha1', @password + replicate('*', 32 - len(@password)))

	select * from dbo.users u
		where u.user_id = @id
		AND u.password = @crypt
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO ELIMINAR USUARIO --------------------------
CREATE or ALTER PROCEDURE dbo.delete_account
(
	@user_id		INT
)
AS
BEGIN
	update u
		set status = 0
		from dbo.users u
		where u.user_id = @user_id
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO OBTENER USUARIO A PARTIR DE SU TOKEN --------------------------
CREATE or ALTER PROCEDURE dbo.find_user_token
(
	@token	VARCHAR(200)
)
AS
BEGIN
	select *
		from dbo.users
		where token_api = @token
		and status = 1
END
GO


-- CURSOR EJEMPLO
DECLARE	@user_id INT

DECLARE cursor_user CURSOR
FOR SELECT user_id
    FROM 
        dbo.users
OPEN cursor_user
FETCH NEXT FROM cursor_user INTO 
    @user_id

WHILE @@FETCH_STATUS = 0
    BEGIN
        update dbo.users
		 set token_api = NEWID()
		 where user_id = @user_id
        FETCH NEXT FROM cursor_user INTO 
            @user_id 
    END;

CLOSE cursor_user;
DEALLOCATE cursor_user;
