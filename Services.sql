use users

drop table dbo.services

-- ################## TABLA SERVICIOS ##################

CREATE TABLE dbo.services
(
    service_id		INT				NOT NULL IDENTITY,
    user_id			INT				NOT NULL,
	url				VARCHAR(500)	NOT NULL,
	protocol        VARCHAR(4)    	NOT NULL,
	reindex			BIT				NOT NULL default 1,
	indexed			BIT				NOT NULL default 0,
	index_date		SMALLDATETIME	NULL,
	isActive		BIT				NOT NULL default 1,
	isUp			BIT				NOT NULL default 1,
	constraint PK__services__END primary key (service_id),
	constraint UK__services__UK_url__END UNIQUE (user_id, url),
	constraint CK__services__valid_protocol__END CHECK (protocol in ('REST', 'SOAP')),
	constraint FK__services__users__END foreign key (user_id) references dbo.users
);
go

select * from dbo.services

-- Para obtener todos los procedimientos almacenados de la base de datos
SELECT *
  FROM users.INFORMATION_SCHEMA.ROUTINES
 WHERE ROUTINE_TYPE = 'PROCEDURE'
go
----------------------------------------------------------------------------------------------------------------

-- LOS SERVICIOS TRATARLOS DE A 1 para asi poder identificar en el metadata a que servicio corresponde cada pagina
-- A LAS PAGINAS QUE TIENE REGISTRADAS UN USUARIO SE PUEDEN TRATAR DE A GRUPO

--execute dbo.get_services

-------------------------- PROCEDIMIENTO ALMACENADO OBTENER SERVICIOS DE USUARIO --------------------------
CREATE OR ALTER PROCEDURE dbo.get_services_user
(
	@user_id		INT
)
AS
BEGIN
	select * from dbo.services s
		WHERE s.user_id = @user_id
END
GO

execute dbo.get_services_user 2

-------------------------- PROCEDIMIENTO ALMACENADO QUE BORRA PÁGINAS DE UN SERVICIO DADO --------------------------
CREATE OR ALTER PROCEDURE dbo.clean_service_pages
(
	@service_id INT
)
as
begin
	update dbo.websites
		set isActive = 0,
			reindex = 0,
			indexed = 0,
			index_date = null
	where service_id = @service_id
end
GO


-------------------------- PROCEDIMIENTO ALMACENADO INSERTAR UN NUEVO SERVICIO --------------------------
CREATE OR ALTER PROCEDURE dbo.insert_service
(
	@user_id		INT,
	@url			VARCHAR(500),
	@protocol       VARCHAR(4)
)
AS
BEGIN
	IF EXISTS (SELECT 1 from dbo.services where url = @url and user_id = @user_id and isActive = 0)
	BEGIN
		update dbo.services
			set isActive = 1,
				reindex = 1,
				indexed = 0,
				index_date = null
			where url = @url and user_id = @user_id
	END
	ELSE
	BEGIN
		insert into dbo.services(user_id, url, protocol)
		values	(@user_id, @url, @protocol)
	END
END
GO


-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR UN SERVICIO --------------------------
CREATE or ALTER PROCEDURE dbo.update_service (
    @id				INT,
    @url		VARCHAR(500),
	@protocol		VARCHAR(4)
)
AS
BEGIN
    update s
	set url = @url,
		protocol = @protocol,
		reindex = 1,
		indexed = 0,
		isUp = 1,
		index_date = null
	from dbo.services s
	where s.service_id = @id
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO PARA OBTENER EL LISTADO DE SERVICIOS (CRAWLER) --------------------------
CREATE or ALTER PROCEDURE dbo.get_services_to_crawl
AS
BEGIN
    select user_id, service_id, url, protocol
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
	@service_id	 INT,
	@get_indexed BIT
)
AS
BEGIN
	IF (@get_indexed = 1)
	BEGIN
		select *
			from dbo.websites
			where service_id = @service_id
			AND	  indexed = 1
	END
	ELSE
	BEGIN
		select *
			from dbo.websites
			where service_id = @service_id
	END
END
GO

----------------------------------------------------------------------------------------------------------------
select user_id, string_agg(url, ',')
	from dbo.services
	where reindex = 1
	group by user_id

insert into dbo.services(user_id, url, protocol, reindex)
values	(1, 'youtube0.com', 'REST', 0),
		(1, 'youtube0.com', 'REST', 0),
		(1, 'youtube0.com', 'REST', 1),
		(1, 'youtube0.com', 'REST', 1),
		(1, 'youtube0.com', 'REST', 1)

-- LOS SERVICIOS TRATARLOS DE A 1 para asi poder identificar en el metadata a que servicio corresponde cada pagina
-- A LAS PAGINAS QUE TIENE REGISTRADAS UN USUARIO SE PUEDEN TRATAR DE A GRUPO



-- TESTING REINDEX, ETC
select * from dbo.services
update dbo.services
	set indexed = 1,
		reindex = 0,
		isActive = 1

select * from dbo.websites

update dbo.websites
	set indexed = 1,
		reindex = 0,
		isActive = 1


select * from dbo.websites
update dbo.websites
	set indexed = 1,
		service_id = 1,
		reindex = 0,
		isActive = 1

select * from dbo.services

select * from dbo.users

select * from dbo.websites