use users

drop table dbo.websites

-- ################## TABLA P�GINAS ##################
CREATE TABLE dbo.websites
(
	website_id		INT				IDENTITY,
    user_id			INT				NOT NULL,
    url		       	VARCHAR(500)	NOT NULL,
    service_id		INT				NULL,
    isActive        BIT				NOT NULL DEFAULT 1,
	indexed			BIT				NOT NULL DEFAULT 0,
	reindex			BIT				NOT NULL DEFAULT 1,
	constraint PK__websites__END primary key (website_id),
	constraint FK__websites__users__END foreign key (user_id) references dbo.users,
    constraint FK__websites__services__END foreign key (service_id) references dbo.services (service_id)
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
	select *
		from dbo.websites w
	where w.user_id = @user_id
	AND   w.isActive = 1
END
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO INSERTAR NUEVA PAGINA --------------------------
CREATE OR ALTER PROCEDURE dbo.new_website
(
	@user_id	INT,
	@url		VARCHAR(500),
	@service_id INT = NULL
)
as
begin
	-- activada, no se puede pisar
	if exists(
		SELECT 1 
		from dbo.websites w
		where w.user_id = @user_id
		AND dbo.get_domain(w.url) = dbo.get_domain(@url)
		AND w.isActive = 1
	)
	BEGIN
		raiserror ('El dominio ya se encuentra registrado',16,1)
		return
	END
	-- eliminada, la volvemos a activar
	if exists(
		SELECT 1 
		from dbo.websites w
		where w.user_id = @user_id
		AND dbo.get_domain(w.url) = dbo.get_domain(@url)
		AND w.isActive = 0
	)
	BEGIN
		update dbo.websites 
			set isActive = 1,
				reindex = 1,
				indexed = 0
		where user_id = @user_id and url = @url
		return
	END
	-- si no existía, se inserta normalmente
	BEGIN
		insert into dbo.websites(user_id, url, service_id)
		values(@user_id, @url, @service_id)
	END	
end
go

		execute dbo.new_website 2, 'http://www.mercadolibre.com'
		execute dbo.new_website 2, 'http://www.mercadolibre1.com'
		execute dbo.new_website 2, 'http://www.mercadolibre2.com'
		execute dbo.new_website 2, 'http://www.mercadolibre3.com'
		execute dbo.new_website 2, 'http://www.mercadolibre4.com'
		execute dbo.new_website 2, 'http://www.mercadolibre5.com'
		execute dbo.new_website 2, 'http://www.mercadolibre6.com'
		execute dbo.new_website 2, 'http://www.mercadolibre7.com'
		execute dbo.new_website 2, 'http://www.mercadolibre8.com'
go
--------------------------------------------------------------------------------------------------------------
-------------------------- FUNCION PARA OBTENER EL DOMINIO DE UNA URL --------------------------
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
select * from dbo.websites

update dbo.websites
	set reindex = 0,
		indexed = 1
	where website_id = 3
-------------------------- PROCEDIMIENTO ALMACENADO SETEAR PAGINA A REINDEXAR --------------------------
CREATE OR ALTER PROCEDURE dbo.reindex
(
	@website_id	INT
)
AS
BEGIN
	update dbo.websites
		set reindex = 1,
			indexed = 0
	where website_id = @website_id
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR PÁGINA --------------------------
CREATE OR ALTER PROCEDURE dbo.update_website
(
	@website_id	INT,
	@url		VARCHAR(500)
)
AS
BEGIN
	update dbo.websites
		set url = @url,
			reindex = 1,
			indexed = 0
	where website_id = @website_id
END
GO
select * from dbo.users

-------------------------- PROCEDIMIENTO ALMACENADO ELIMINAR PAGINA --------------------------
CREATE OR ALTER PROCEDURE dbo.delete_website
(
	@website_id	INT
)
AS
BEGIN
	if exists(SELECT * from dbo.websites w where w.website_id = @website_id)
	BEGIN
		update w 
			set isActive = 0
		from dbo.websites w
		where w.website_id = @website_id
	END
	ELSE
	BEGIN
		raiserror ('La operacion no se pudo realizar porque la pagina o el usuario no existen',16,1)
	END
END
GO
select * from dbo.websites
-------------------------- PROCEDIMIENTO ALMACENADO CHEQUEAR SI DOMINIO EXISTE --------------------------
CREATE OR ALTER PROCEDURE dbo.find_website
(
	@website_id	INT
)
AS
BEGIN
	select * from dbo.websites
		where website_id = @website_id
END
GO

execute dbo.find_website 1

-------------------------- PROCEDIMIENTO ALMACENADO CHEQUEAR SI DOMINIO YA ESTA REGISTRADO --------------------------
CREATE OR ALTER PROCEDURE dbo.check_domain
(
	@user_id	INT,
	@url		VARCHAR(500),
	@website_id	INT = NULL
)
AS
BEGIN
	declare @domain varchar(500)
	set @domain = dbo.get_domain(@url)

	IF (@website_id IS NULL)
	BEGIN
		select * from dbo.websites w
			where w.user_id = @user_id
			AND dbo.get_domain(w.url) = @domain
			AND w.isActive = 1
	END
	ELSE
	BEGIN
		select * from dbo.websites w
			where w.user_id = @user_id
			AND dbo.get_domain(w.url) = @domain
			AND w.website_id != @website_id
			AND w.isActive = 1
	END
END
GO

execute dbo.check_domain 2, 'http://www.mercadolibre.com'

execute dbo.delete_website 2, 'mercadolibre9.com'

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
