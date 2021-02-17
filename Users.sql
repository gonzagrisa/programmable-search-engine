use users

drop table dbo.users
drop table dbo.websites
drop table dbo.services
drop table dbo.preferences

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
	constraint UK__users__END unique (username),
	constraint CK__users_role__END check (role in ('ADMIN', 'USER'))
);
go

/*
* *****************************************
*	PROCEDIMIENTOS ALMACENADOS
* *****************************************
*/

-------------------------- PROCEDIMIENTO ALMACENADO OBTENER TODOS LOS USUARIOS ACTIVOS --------------------------
create or alter procedure dbo.get_users
as
begin
	select * from dbo.users u
	where u.role = 'USER'
	and u.status = 1
end
go

-------------------------- PROCEDIMIENTO ALMACENADO CHEQUEAR SI NOMBRE DE USUARIO ESTA EN USO --------------------------
create or alter procedure dbo.check_username
(
	@username	varchar(50),
	@user_id	INT = -1
)
as
begin	
	select * from dbo.users u
		where u.username = @username
		and u.user_id != @user_id
		and u.status = 1
end
go

execute dbo.check_username 'admin', 1
go

-------------------------- PROCEDIMIENTO ALMACENADO OBTENER DATOS DE UN USUARIO --------------------------
create or alter procedure dbo.get_user_info
(
	@user_id	INT
)
as
begin
	select * from dbo.users
		where user_id = @user_id
end
go

execute dbo.get_user_info 1
go

-------------------------- PROCEDIMIENTO ALMACENADO LOGIN --------------------------
create or alter procedure dbo.validate_user
(
	@username	varchar(50),
	@password	varchar(50)
)
as
begin	
	declare @crypt varbinary(32)
	select @crypt = HASHBYTES('sha1', @password + replicate('*', 32 - len(@password)))

	select * from dbo.users u
		where u.username = @username
		and   u.password = @crypt
		and status = 1
end
go

select * from dbo.users

execute dbo.validate_user 'admin', 'secret'
go
-------------------------- PROCEDIMIENTO ALMACENADO REGISTRAR --------------------------
create or alter procedure dbo.new_user
(
	@name		varchar(50),
	@last_name	varchar(50),
	@username	varchar(50),
	@password	varchar(50),
	@role		varchar(20) = 'USER'
)
as
begin	
	declare @crypt varbinary(32)
	select @crypt = HASHBYTES('sha1', @password + replicate('*', 32 - len(@password)))

	insert into dbo.users (name, last_name, username, password, role, token_api, status)
	values	(@name, @last_name, @username, @crypt, @role, null, 1)	
end
go


execute dbo.new_user 'userName', 'userSurname', 'user3', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user4', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user5', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user6', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user7', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user8', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user9', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user10', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user11', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user12', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user13', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user14', 'secret'
execute dbo.new_user 'userName', 'userSurname', 'user15', 'secret'
go


-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR INFORMACION USUARIO --------------------------
CREATE or ALTER PROCEDURE dbo.update_user
    @id			INT,
	@username	VARCHAR(50) = null,
	@name		VARCHAR(50) = null,
    @last_name	VARCHAR(50) = null,
	@password	VARCHAR(50) = null
AS
BEGIN
	
	if (@password IS NOT null)
	begin
		declare @crypt varbinary(32)
		select @crypt = HASHBYTES('sha1', @password + replicate('*', 32 - len(@password)))

		update u
		set password = @crypt
		from dbo.users u
		where u.user_id = @id
	end
	if (@name IS NOT null AND @last_name IS NOT null AND @username IS NOT null)
	begin
		update u
			set name = @name,
				last_name = @last_name,
				username = @username
			from dbo.users u
			where u.user_id = @id
	end
END
GO

execute dbo.update_user 5, 'samuele1', 'samuele1', 'samuele1'
select * from dbo.users
select * from preferences
execute dbo.new_user 'admin', 'admin', 'admin', 'admin', 'ADMIN'
go


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

select * from dbo.users
execute dbo.check_password 1, 'admin1'


select * from dbo.users
go


-------------------------- PROCEDIMIENTO ALMACENADO ELIMINAR USUARIO --------------------------
create or alter procedure dbo.delete_account
(
	@user_id		INT
)
as
begin
	update u
		set status = 0
		from dbo.users u
		where u.user_id = @user_id
end
go

-------------------------- PROCEDIMIENTO ALMACENADO OBTENER USUARIO A PARTIR DE SU TOKEN --------------------------
create or alter procedure dbo.find_user_token
(
	@token	VARCHAR(200)
)
as
begin
	select *
		from dbo.users
		where token_api = @token
		and status = 1
end
go


select * from dbo.users
	where token_api = 'A13E8731-120F-4586-8466-BC11BD51BC49'

execute dbo.find_user_token 'A13E8731-120F-4586-8466-BC11BD51BC49'


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
