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
	token_api	UNIQUEIDENTIFIER default NEWID() NULL,
	constraint PK__users__END primary key (user_id),
	constraint UK__users__END unique (username),
	constraint CK__users_role__END check (role in ('ADMIN', 'USER'))
);
go

-- ################## TABLA PREFERENCIAS ##################
CREATE TABLE dbo.preferences
(
	user_id			INT				NOT NULL,
	color			VARCHAR(50)		NOT NULL,
	icon_url		VARCHAR(500)	NOT NULL,
	border_radius	INT				NOT NULL,
	font_size		TINYINT			NOT NULL,
	constraint PK__preferences__END primary key (user_id),
	constraint FK__preferences__users__END foreign key (user_id) references dbo.users on delete cascade
);
go

-- ################## TABLA SERVICIOS ##################
CREATE TABLE dbo.services
(
    user_id			INT				NOT NULL,
    url_resource	VARCHAR(500)	NOT NULL,
	url_ping		VARCHAR(500)	NOT NULL,
	reindex			TINYINT			NOT NULL default 1,
	constraint PK__services__END primary key (user_id, url_resource),
	constraint FK__services__users__END foreign key (user_id) references dbo.users on delete cascade
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
end
go

select * from dbo.users

execute dbo.validate_user 'admin', 'secret'

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


execute dbo.new_user 'user2', 'user2', 'user3', 'secret'
execute dbo.new_user 'user2', 'user2', 'user4', 'secret'
execute dbo.new_user 'user2', 'user2', 'user5', 'secret'
execute dbo.new_user 'user2', 'user2', 'user6', 'secret'
execute dbo.new_user 'user2', 'user2', 'user7', 'secret'
execute dbo.new_user 'user2', 'user2', 'user8', 'secret'
execute dbo.new_user 'user2', 'user2', 'user9', 'secret'
execute dbo.new_user 'user2', 'user2', 'user10', 'secret'
execute dbo.new_user 'user2', 'user2', 'user11', 'secret'
execute dbo.new_user 'user2', 'user2', 'user12', 'secret'
execute dbo.new_user 'user2', 'user2', 'user13', 'secret'
execute dbo.new_user 'user2', 'user2', 'user14', 'secret'
execute dbo.new_user 'user2', 'user2', 'user15', 'secret'
go

-------------------------- TRIGGER PARA INSERTAR LAS OPCIONES POR DEFECTO AL NUEVO USUARIO CREADO --------------------------
create trigger ti_users
on dbo.users
for insert
as
begin
	declare @id INT
	set @id = (select user_id from inserted)
	insert into dbo.preferences(user_id, color, icon_url, border_radius, font_size)
	values (@id, 'blue', 'https://cdn2.iconfinder.com/data/icons/font-awesome/1792/search-512.png', 50, 14)
end
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


-------------------------- PROCEDIMIENTO ALMACENADO CHEQUEAR CONTRASEÑA ANTIGÜA --------------------------
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

-------------------------- PROCEDIMIENTO ALMACENADO OBTENER PREFERENCIAS USUARIO --------------------------
create or alter procedure dbo.get_preferences
(
	@user_id	INT
)
as
begin
	select * from dbo.preferences p
		where p.user_id = @user_id
end
go

-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR PREFERENCIAS BUSCADOR --------------------------
create or alter procedure dbo.update_preferences
(
	@user_id		INT,
	@color			VARCHAR(50),
	@icon_url		VARCHAR(500),
	@border_radius	INT,
	@font_size		INT
)
as
begin
	update p
		set p.color = @color,
			p.icon_url = @icon_url,
			p.border_radius = @border_radius,
			p.font_size = @font_size
		from dbo.preferences p
		where p.user_id = @user_id
end
go

select * from dbo.users

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

select * from dbo.users


















insert into dbo.websites(user_id, url, reindex)
values	(1, 'youtube.com', 0)

insert into dbo.websites(user_id, url, reindex)
values	(1, 'youtube0.com', 0),
		(1, 'youtube1.com', 1),
		(1, 'youtube2.com', 1),
		(1, 'youtube3.com', 1),
		(1, 'youtube4.com', 1),
		(2, 'youtube0.com', 0),
		(2, 'youtube1.com', 0),
		(2, 'youtube2.com', 1),
		(2, 'youtube3.com', 1),
		(2, 'youtube4.com', 1)

select user_id, url
	from dbo.websites
	where reindex = 1
	group by user_id, url
	order by user_id


select user_id, string_agg(url, ',')
	from dbo.websites
	where reindex = 1
	group by user_id

-- DOMINIO, www.youtube.com/*
-- AL PONER PARA REINDEXAR UNA PAGINA, SE TIENE QUE BORRAR TODO LO QUE EMPIECE CON EL DOMINIO DE ESA PAGINA

--------------------------------------------------------------------------------------------------------------------------------------------
select user_id, string_agg(url_resource, ',')
	from dbo.services
	where reindex = 1
	group by user_id

select * from dbo.services
	where reindex = 1

select * from dbo.services

insert into dbo.services(user_id, url_resource, url_ping, reindex)
values	(1, 'youtube0.com/ping0','youtube0.com/ping', 0),
		(1, 'youtube0.com/ping1','youtube0.com/ping', 0),
		(1, 'youtube0.com/ping2','youtube0.com/ping', 1),
		(1, 'youtube0.com/ping3','youtube0.com/ping', 1),
		(1, 'youtube0.com/ping4','youtube0.com/ping', 1),
		(2, 'youtube0.com/ping0','youtube0.com/ping', 0),
		(2, 'youtube0.com/ping1','youtube0.com/ping', 0),
		(2, 'youtube0.com/ping2','youtube0.com/ping', 1),
		(2, 'youtube0.com/ping3','youtube0.com/ping', 1),
		(2, 'youtube0.com/ping4','youtube0.com/ping', 1)

-- LOS SERVICIOS TRATARLOS DE A 1 para asi poder identificar en el metadata a que servicio corresponde cada pagina
-- A LAS PAGINAS QUE TIENE REGISTRADAS UN USUARIO SE PUEDEN TRATAR DE A GRUPO