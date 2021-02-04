use users

drop table dbo.websites

-- ################## TABLA P�GINAS ##################
CREATE TABLE dbo.websites
(
    user_id			INT				NOT NULL,
    url		       	VARCHAR(500)	NOT NULL,
    service_id		INT				NULL,
    isActive        BIT				NOT NULL DEFAULT 1,
	indexed			BIT				NOT NULL DEFAULT 0,
	reindex			BIT				NOT NULL DEFAULT 1,																							-- Agregar Campo Indexed (para el frontend)
	constraint PK__websites__END primary key (user_id, url),																					-- Va a haber problemas cuando se haga el delete y despues se inserte de nuevo la misma url
	constraint FK__websites__users__END foreign key (user_id) references dbo.users,
    constraint FK__websites__services__END foreign key (user_id, service_id) references dbo.services (user_id, service_id) on delete cascade
);
go


-- execute dbo.get_websites 1
-- select * from dbo.websites

insert into dbo.websites(user_id, url)
values	(1, 'https://www.youtube.com')
go

-------------------------- PROCEDIMIENTO ALMACENADO SELECCIONAR PÁGINAS POR USUARIO --------------------------
-- Este procedimiento se usa tanto del crawler (user_id = NULL) como del ABM (user_id != NULL)
CREATE OR ALTER PROCEDURE dbo.get_websites (@user_id int = NULL)
AS
BEGIN
IF (@user_id IS NULL)
BEGIN
	select user_id, string_agg(url, ',') as websites
	from dbo.websites
	where reindex = 1 and isActive = 1
	group by user_id
END
IF (@user_id IS NOT NULL)
BEGIN
	select user_id, url, isActive, reindex, indexed
		from dbo.websites w
	where w.user_id = @user_id
END
END
go

-- execute dbo.get_websites @user_id = 2

update w
	set reindex = 0
	from dbo.websites w
	where w.url = 'mercadolibre9.com'
-------------------------- PROCEDIMIENTO ALMACENADO INSERTAR NUEVA PAGINA --------------------------
CREATE OR ALTER PROCEDURE dbo.new_website
(
	@user_id	INT,
	@url		VARCHAR(500),
	@service_id INT = NULL
)
as
begin
	if exists(
		SELECT 1 
		from dbo.websites w
		where w.user_id = @user_id
		AND dbo.get_domain(w.url) = dbo.get_domain(@url)
	)
	BEGIN
		raiserror ('El dominio ya se encuentra registrado',16,1)
		return
	END
	ELSE
	BEGIN
		insert into dbo.websites(user_id, url, service_id)
		values(@user_id, @url, @service_id)
	END	
end
go

execute dbo.new_website 2, 'mercadolibre.com'
execute dbo.new_website 2, 'mercadolibre2.com'
execute dbo.new_website 2, 'mercadolibre3.com'
execute dbo.new_website 2, 'mercadolibre4.com'
execute dbo.new_website 2, 'mercadolibre5.com'
execute dbo.new_website 2, 'mercadolibre6.com'
execute dbo.new_website 2, 'mercadolibre7.com'
execute dbo.new_website 2, 'mercadolibre8.com'
execute dbo.new_website 2, 'mercadolibre9.com'
--------------------------------------------------------------------------------------------------------------

CREATE or ALTER FUNCTION dbo.get_domain (@url VARCHAR(500))
RETURNS VARCHAR(500)
AS BEGIN
    DECLARE @domain varchar(500)
	SET @domain = 
		/* Get just the host name from a URL */
		SUBSTRING(@url,
			/* Starting Position (After any '//') */
			(CASE WHEN CHARINDEX('//', @url) = 0 THEN 1 ELSE CHARINDEX('//', @url) + 2 END),
			/* Length (ending on first '/' or on a '?') */
			CASE
				WHEN CHARINDEX('/', @url, CHARINDEX('//', @url) + 2) > 0 THEN CHARINDEX('/', @url, CHARINDEX('//', @url) + 2) - (CASE WHEN CHARINDEX('//', @url)= 0 THEN 1 ELSE CHARINDEX('//', @url) + 2 END)
				WHEN CHARINDEX('?', @url, CHARINDEX('//', @url) + 2) > 0 THEN CHARINDEX('?', @url, CHARINDEX('//', @url) + 2) - (CASE WHEN CHARINDEX('//', @url)= 0 THEN 1 ELSE CHARINDEX('//', @url) + 2 END)
				ELSE LEN(@url)
			END)
	return @domain
END
go

/*
CREATE or ALTER FUNCTION dbo.valid_domain (@url VARCHAR(500))
RETURNS BIT
AS BEGIN
	if (CHARINDEX('http://', @url) = 1
		OR CHARINDEX('https://', @url) = 1
		OR CHARINDEX('www.', @url) = 1
		OR @url LIKE '[1-250].[0-250].[0-250].[0-250]'
	)

	begin
		return 1
	end
	return 0
END
go
*/

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
		AND url = @url
	END
	ELSE
	BEGIN
		raiserror ('La operacion no se pudo realizar porque la pagina o el usuario no existen',16,1)
	END
end
go

execute dbo.delete_website 1, 'www.mercadolibre.com.ar/about'

select * from dbo.websites

----------------------------------------------------------------------------------------------------------------

select user_id, url
	from dbo.websites
	where reindex = 1
	group by user_id, url
	order by user_id


select user_id, string_agg(url, ',') as pages
	from dbo.websites
	where reindex = 1 and isActive = 1
	group by user_id

-- DOMINIO, www.youtube.com/*
-- AL PONER PARA REINDEXAR UNA PAGINA, SE TIENE QUE BORRAR TODO LO QUE EMPIECE CON EL DOMINIO DE ESA PAGINA
