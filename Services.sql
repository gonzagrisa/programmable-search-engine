use users

drop table dbo.services

-- ################## TABLA SERVICIOS ##################

CREATE TABLE dbo.services
(
    service_id		INT				NOT NULL IDENTITY,
    user_id			INT				NOT NULL,
    url_resource	VARCHAR(500)	NOT NULL,
	url_ping		VARCHAR(500)	NOT NULL,
	protocol        VARCHAR(4)    	NOT NULL,
	reindex			BIT				NOT NULL default 1,
	indexed			BIT				NOT NULL default 0,
	index_date		SMALLDATETIME	NULL,
	isActive		BIT				NOT NULL default 1,
	isUp			BIT				NOT NULL default 1,
	constraint PK__services__END primary key (service_id),
	constraint UK__services__UK_url_resource__END UNIQUE (user_id, url_resource),
	constraint CK__services__valid_protocol__END CHECK (protocol in ('REST', 'SOAP')),
	constraint FK__services__users__END foreign key (user_id) references dbo.users
);
go

select * from dbo.services

-- Para obtener todos los procedimientos almacenados de la base de datos
SELECT * 
  FROM users.INFORMATION_SCHEMA.ROUTINES
 WHERE ROUTINE_TYPE = 'PROCEDURE'

----------------------------------------------------------------------------------------------------------------
execute dbo.get_services_to_crawl
go
-------------------------- PROCEDIMIENTO ALMACENADO OBTENER SERVICIOS DE USUARIO --------------------------
CREATE OR ALTER PROCEDURE dbo.get_services_user
(
	@user_id		INT
)
AS
BEGIN
	select * from dbo.services s
		WHERE s.user_id = @user_id
		AND s.isActive = 1
END
GO

execute dbo.get_services_user 3



go
-------------------------- PROCEDIMIENTO ALMACENADO INSERTAR UN NUEVO SERVICIO --------------------------
CREATE OR ALTER PROCEDURE dbo.insert_service
(
	@user_id		INT,
	@url_ping		VARCHAR(500),
	@url_resource	VARCHAR(500),
	@protocol       VARCHAR(4)
)
AS
BEGIN
	insert into dbo.services(user_id, url_resource, url_ping, protocol)
	values	(@user_id, @url_resource, @url_ping, @protocol)
END
GO

execute dbo.insert_service 2, 'http://desktop-a0iuvm3:8088/asdad', 'http://desktop-a0iuvm3:8088/asdad1', 'REST'

select * from dbo.services


-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR UN SERVICIO --------------------------
CREATE or ALTER PROCEDURE dbo.update_service (
    @id				INT,
	@url_resource	VARCHAR(500),
    @url_ping		VARCHAR(500),
	@protocol		VARCHAR(4)
)
AS
BEGIN
    update s
	set url_resource = @url_resource,
		url_ping = @url_ping,
		protocol = @protocol,
		reindex = 1,
		indexed = 0,
		isUp = 1,
		index_date = null
	from dbo.services s
	where s.service_id = @id
END
GO

execute dbo.get_services_to_crawl
-------------------------- PROCEDIMIENTO ALMACENADO PARA OBTENER EL LISTADO DE SERVICIOS (CRAWLER) --------------------------
CREATE or ALTER PROCEDURE dbo.get_services_to_crawl
AS
BEGIN
    select user_id, service_id, url_resource, url_ping, protocol
	from dbo.services
	where reindex = 1
	 and isActive = 1
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO PARA REGISTRAR UN SERVICIO COMO CAÍDO --------------------------
CREATE or ALTER PROCEDURE dbo.update_service_status (
	@service_id int,
	@status		BIT
)
AS
BEGIN
	update dbo.services
		set isUp = @status
		where service_id = @service_id
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO PARA ACTUALIZAR REINDEX DE SERVICIO --------------------------
-- PONER REINDEX = 0 si ya se lo esta indexando (crawler), 1 si se lo quiere reindexar (usuario)
CREATE OR ALTER PROCEDURE dbo.update_reindex_status
(
	@service_id	INT,
	@reindex	BIT
)
AS
BEGIN
	-- if true, servicio a reindexar
	IF (@reindex = 1)
	BEGIN
		update dbo.services
		set	reindex = 1,
			indexed = 0,
			isUp = 1,
			index_date = NULL
		where service_id = @service_id
	END
	-- if false, servicio a no reindexar
	IF (@reindex = 0)
	BEGIN
		update dbo.services
		set reindex = 0
		where service_id = @service_id
	END
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO PARA ELIMINAR SERVICIO --------------------------
CREATE OR ALTER PROCEDURE dbo.delete_service
(
	@service_id	INT
)
AS
BEGIN
	update dbo.services
		set isActive = 0
	where service_id = @service_id
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO PARA OBTENER TODAS LAS PAGINAS REGISTRADAS DE UN SERVICIO --------------------------
CREATE OR ALTER PROCEDURE dbo.get_service_websites
(
	@service_id	INT
)
AS
BEGIN
	select * 
		from dbo.websites
		where @service_id = @service_id
END
GO

execute dbo.get_service_websites 1

----------------------------------------------------------------------------------------------------------------
select user_id, string_agg(url_resource, ',')
	from dbo.services
	where reindex = 1
	group by user_id

insert into dbo.services(user_id, url_resource, url_ping, protocol, reindex)
values	(1, 'youtube0.com/0','youtube0.com/ping', 'REST', 0),
		(1, 'youtube0.com/1','youtube0.com/ping', 'REST', 0),
		(1, 'youtube0.com/2','youtube0.com/ping', 'REST', 1),
		(1, 'youtube0.com/3','youtube0.com/ping', 'REST', 1),
		(1, 'youtube0.com/4','youtube0.com/ping', 'REST', 1)

-- LOS SERVICIOS TRATARLOS DE A 1 para asi poder identificar en el metadata a que servicio corresponde cada pagina
-- A LAS PAGINAS QUE TIENE REGISTRADAS UN USUARIO SE PUEDEN TRATAR DE A GRUPO
