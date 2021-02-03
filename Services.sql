use users

drop table dbo.services

-- ################## TABLA SERVICIOS ##################
CREATE TABLE dbo.services
(
    user_id			INT				NOT NULL,
    service_id		INT				NOT NULL IDENTITY,
    url_resource	VARCHAR(500)	NOT NULL,
	url_ping		VARCHAR(500)	NOT NULL,
	protocol        VARCHAR(4)    	NOT NULL,
	reindex			TINYINT			NOT NULL default 1,
	isActive		TINYINT			NOT NULL default 1,
	isUp			TINYINT			NOT NULL default 1,
	constraint PK__services__END primary key (user_id, service_id),
	constraint PK__services__UK_service_id__END UNIQUE (service_id),
	constraint PK__services__UK_url_resource__END UNIQUE (url_resource),
	constraint PK__services__valid_protocol__END CHECK (protocol in ('REST', 'SOAP')),
	constraint FK__services__users__END foreign key (user_id) references dbo.users on delete cascade
);
go

----------------------------------------------------------------------------------------------------------------
select user_id, string_agg(url_resource, ',')
	from dbo.services
	where reindex = 1
	group by user_id

-- select * from dbo.services

-- delete from dbo.services
go
-- LOS SERVICIOS TRATARLOS DE A 1 para asi poder identificar en el metadata a que servicio corresponde cada pagina
-- A LAS PAGINAS QUE TIENE REGISTRADAS UN USUARIO SE PUEDEN TRATAR DE A GRUPO

-------------------------- PROCEDIMIENTO ALMACENADO REGISTRAR UN NUEVO SERVICIO --------------------------
create or alter procedure dbo.insert_service
(
	@user_id		INT,
	@url_resource	VARCHAR(500),
	@url_ping		VARCHAR(500),
	@protocol       VARCHAR(4)
)
as
begin
	insert into dbo.services(user_id, url_resource, url_ping, protocol)
	values	(@user_id, @url_resource, @url_ping, @protocol)
end
go

-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR UN SERVICIO --------------------------
CREATE or ALTER PROCEDURE dbo.update_service
    @id				INT,
	@url_resource	VARCHAR(500),
    @url_ping		VARCHAR(500),
	@protocol		VARCHAR(4)
AS
BEGIN
    update s
	set url_resource = @url_resource,
		url_ping = @url_ping,
		protocol = @protocol,
		reindex = 1
	from dbo.services s
	where s.user_id = @id and s.url_resource = @url_resource
END
GO
--------------------------------------------------------------------------------------------------------------------------------------------
select user_id, string_agg(url_resource, ',')
	from dbo.services
	where reindex = 1
	group by user_id

select * from dbo.services
	where reindex = 1

insert into dbo.services(user_id, url_resource, url_ping, protocol, reindex)
values	(1, 'youtube0.com/0','youtube0.com/ping', 'REST', 0),
		(1, 'youtube0.com/1','youtube0.com/ping', 'REST', 0),
		(1, 'youtube0.com/2','youtube0.com/ping', 'REST', 1),
		(1, 'youtube0.com/3','youtube0.com/ping', 'REST', 1),
		(1, 'youtube0.com/4','youtube0.com/ping', 'REST', 1)

-- LOS SERVICIOS TRATARLOS DE A 1 para asi poder identificar en el metadata a que servicio corresponde cada pagina
-- A LAS PAGINAS QUE TIENE REGISTRADAS UN USUARIO SE PUEDEN TRATAR DE A GRUPO