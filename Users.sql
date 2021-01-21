drop table dbo.users
drop table dbo.websites
drop table dbo.services
drop table dbo.preferences

-------------------------- TABLA USUARIOS --------------------------
CREATE TABLE dbo.users (
	user_id		INT IDENTITY(1,1)	NOT NULL,
	username	VARCHAR(50)			NOT NULL,
	name		VARCHAR(50)			NOT NULL,
	last_name	VARCHAR(50)			NOT NULL,
	password	VARBINARY(32)		NOT NULL,
	role		VARCHAR(20)			NOT NULL,
	token_api	UNIQUEIDENTIFIER default NEWID() NULL,
	constraint PK__users__END primary key (user_id),
	constraint UK__users__END unique (username),
	constraint CK__users_role__END check (role in ('ADMIN', 'USER'))
);
go

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

CREATE TABLE dbo.preferences
(
	user_id		INT				NOT NULL,
	color		VARCHAR(50)		NOT NULL,
	icon_url	VARCHAR(500)	NOT NULL,
	border_radius DECIMAL(4,2)	NOT NULL,
	font_size	TINYINT			NOT NULL,
	constraint PK__preferences__END primary key (user_id),
	constraint FK__preferences__users__END foreign key (user_id) references dbo.users on delete cascade
);
go

CREATE TABLE dbo.websites
(
    user_id	INT				NOT NULL,
    url		VARCHAR(500)	NOT NULL,
	reindex	TINYINT			NOT NULL,
	constraint PK__websites__END primary key (user_id, url),
	constraint FK__websites__users__END foreign key (user_id) references dbo.users on delete cascade
);
go

CREATE TABLE dbo.crawling_stats
(
	crawl_id		INT IDENTITY(1,1)	NOT NULL,
	user_id			INT					NOT NULL,
	url_visitadas	INT					NOT NULL,
	url_skipped		INT					NOT NULL,

	parsing_fail	INT					NOT NULL,
);
go

-------------------------- PROCEDIMIENTO ALMACENADO VALIDAR USUARIO --------------------------
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

-------------------------- PROCEDIMIENTO ALMACENADO CREAR NUEVO USUARIO --------------------------
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

	insert into dbo.users (name, last_name, username, password, role, token_api)
	values	(@name, @last_name, @username, @crypt, @role, null)	
end
go

-------------------------- TRIGGER PARA SETTEAR LAS OPCIONES POR DEFECTO AL NUEVO USUARIO --------------------------
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
CREATE PROCEDURE dbo.update_user 
    @id			INT,
	@name		VARCHAR(50),
    @last_name	VARCHAR(50),
	@username	VARCHAR(50),
	@password	VARCHAR(50)
AS
BEGIN
	declare @crypt varbinary(32)
	select @crypt = HASHBYTES('sha1', @password + replicate('*', 32 - len(@password)))

    update u
	set name = @name,
		last_name = @last_name,
		username = @username,
		password = @crypt
	from dbo.users u
	where u.user_id = @id
END
GO

select * from dbo.users

declare @id int = 1, @name varchar(50) = 'admin', @last_name varchar(50) = 'admin', @username varchar(50) = 'admin', @password varchar (50) = 'admin'
execute dbo.update_user @id, @name, @last_name, @username, @password

declare @name varchar(50) = 'admin', @last_name varchar(50) = 'admin', @username varchar(50) = 'admin', @password varchar (50) = 'admin', @role varchar(20) = 'ADMIN'
execute dbo.new_user @name=@name, @last_name=@last_name, @username=@username, @password=@password, @role=@role


-------------------------- PROCEDIMIENTO ALMACENADO INSERTAR NUEVA PAGINA --------------------------
create or alter procedure dbo.new_website
(
	@user_id	INT,
	@url		VARCHAR(500)
)
as
begin
	if exists(SELECT 1 from dbo.users where user_id = @user_id)
	BEGIN
		insert into dbo.websites(user_id, url)
		values(@user_id, @url)
	END
	ELSE
	BEGIN
		raiserror ('El usuario no existe',16,1)
	END
end
go

-------------------------- PROCEDIMIENTO ALMACENADO ELIMINAR PAGINA --------------------------
create or alter procedure dbo.delete_website
(
	@user_id	INT,
	@url		VARCHAR(500)
)
as
begin
	if exists(SELECT * from dbo.websites w where w.user_id = @user_id AND w.url = @url)
	BEGIN
		delete from 
			dbo.websites
		where user_id = @user_id
	END
	ELSE
	BEGIN
		raiserror ('La operacion no se pudo realizar porque la pagina o el usuario no existen',16,1)
	END
end
go


user_id	INT				NOT NULL,
url		VARCHAR(500)	NOT NULL,
reindex	TINYINT			NOT NULL,

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

-- DOMAIN, www.youtube.com/*
-- AL PONER PARA REINDEXAR UNA PAGINA, SE TIENE QUE BORRAR TODO LO QUE EMPIECE CON EL DOMINIO DE ESA PAGINA


--------------------------------------------------------------------------------------------------------------------------------------------
select user_id, string_agg(url_resource, ',')
	from dbo.services
	where reindex = 1
	group by user_id

select * from dbo.services
	where reindex = 1

select * from dbo.services
 user_id			INT				NOT NULL,
    url_resource	VARCHAR(500)	NOT NULL,
	url_ping		VARCHAR(500)	NOT NULL,
	reindex			TINYINT			NOT NU

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