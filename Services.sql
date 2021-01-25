use users

-- ################## TABLA SERVICIOS ##################
CREATE TABLE dbo.services
(
    user_id			INT				NOT NULL,
    service_id		INT				NOT NULL IDENTITY,
    url_resource	VARCHAR(500)	NOT NULL,
	url_ping		VARCHAR(500)	NOT NULL,
	reindex			TINYINT			NOT NULL default 1,
	constraint PK__services__END primary key (user_id, url_resource),
	constraint FK__services__users__END foreign key (user_id) references dbo.users on delete cascade
);
go

----------------------------------------------------------------------------------------------------------------
select user_id, string_agg(url_resource, ',')
	from dbo.services
	where reindex = 1
	group by user_id

select * from dbo.services
	where reindex = 1

select * from dbo.services
go
-- LOS SERVICIOS TRATARLOS DE A 1 para asi poder identificar en el metadata a que servicio corresponde cada pagina
-- A LAS PAGINAS QUE TIENE REGISTRADAS UN USUARIO SE PUEDEN TRATAR DE A GRUPO

create or alter procedure dbo.insert_service
(
	@user_id		INT,
	@url_resource	VARCHAR(50),
	@url_ping		VARCHAR(500)
)
as
begin
	insert into dbo.services(user_id, url_resource, url_ping)
	values	(@user_id, @url_resource, @url_ping)
end
go

--------------------------------------------------------------------------------------------------------------------------------------------
select user_id, string_agg(url_resource, ',')
	from dbo.services
	where reindex = 1
	group by user_id

select * from dbo.services
	where reindex = 1

insert into dbo.services(user_id, url_resource, url_ping, reindex)
values	(1, 'youtube0.com/0','youtube0.com/ping', 0),
		(1, 'youtube0.com/1','youtube0.com/ping', 0),
		(1, 'youtube0.com/2','youtube0.com/ping', 1),
		(1, 'youtube0.com/3','youtube0.com/ping', 1),
		(1, 'youtube0.com/4','youtube0.com/ping', 1)
		-- (2, 'youtube0.com/ping0','youtube0.com/ping', 0),
		-- (2, 'youtube0.com/ping1','youtube0.com/ping', 0),
		-- (2, 'youtube0.com/ping2','youtube0.com/ping', 1),
		-- (2, 'youtube0.com/ping3','youtube0.com/ping', 1),
		-- (2, 'youtube0.com/ping4','youtube0.com/ping', 1)

-- LOS SERVICIOS TRATARLOS DE A 1 para asi poder identificar en el metadata a que servicio corresponde cada pagina
-- A LAS PAGINAS QUE TIENE REGISTRADAS UN USUARIO SE PUEDEN TRATAR DE A GRUPO