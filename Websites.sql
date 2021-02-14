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
	index_date		SMALLDATETIME	NULL,			
	reindex			BIT				NOT NULL DEFAULT 1,
	isUp			BIT				NOT NULL DEFAULT 1,
	constraint PK__websites__END primary key (website_id),
	constraint FK__websites__users__END foreign key (user_id) references dbo.users,
    constraint FK__websites__services__END foreign key (service_id) references dbo.services (service_id)
);
go

GO
-------------------------- PROCEDIMIENTO ALMACENADO SELECCIONAR PÁGINAS POR USUARIO --------------------------
-- Este procedimiento se usa tanto del crawler (user_id = NULL) como del ABM (user_id != NULL)
CREATE OR ALTER PROCEDURE dbo.get_websites (@user_id int = NULL)
AS
BEGIN
	IF (@user_id IS NULL)
	BEGIN
		--select user_id, string_agg(url, ',') as websites
		select user_id, STRING_AGG(url, ',') as websites, STRING_AGG(website_id, ',') as websites_id
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

-------------------------- PROCEDIMIENTO ALMACENADO CHEQUEAR SI DOMINIO EXISTE (PARA EL DELETE DE WEBSITE, CHEQUEAR SI EXISTE) --------------------------
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

-------------------------- PROCEDIMIENTO ALMACENADO INSERTAR NUEVA PAGINA --------------------------
CREATE OR ALTER PROCEDURE dbo.insert_website
(
	@user_id		INT,
	@url			VARCHAR(500),
	@affectedRows	INT OUTPUT
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
		update dbo.websites set isActive = 1, reindex = 1, indexed = 0, isUp = 1 
			where user_id = @user_id and url = @url
		return
	END
	-- si no existía, se inserta normalmente
	BEGIN
		insert into dbo.websites(user_id, url)
		values(@user_id, @url)
	END
	SELECT @affectedRows = @@ROWCOUNT;
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO INSERTAR NUEVA PAGINA DESDE UN SERVICIO --------------------------
CREATE OR ALTER PROCEDURE dbo.new_website_from_service
(
	@user_id	INT,
	@url		VARCHAR(500),
	@service_id INT
)
as
begin
	-- si ya existe un dominio con el mismo servicio y mismo dominio, usar ese
	IF EXISTS(
		SELECT 1
		from dbo.websites
		where user_id = @user_id
		AND dbo.get_domain(url) = dbo.get_domain(@url)
		AND (service_id = @service_id OR (service_id is null and indexed = 0))
	)
	BEGIN
		update dbo.websites
			set reindex = 1,
				indexed = 0,
				isActive = 1,
				index_date = null,
				service_id = @service_id
			where dbo.get_domain(url) = dbo.get_domain(@url)
			AND	  user_id = @user_id
	END
	-- si ya existe un dominio con el mismo dominio pero que fue insertado a mano y esta indexado o fue insertado desde otro servicio, no insertarlo
	ELSE IF EXISTS( SELECT 1
				from dbo.websites
				where user_id = @user_id
				AND dbo.get_domain(url) = dbo.get_domain(@url)
				AND (service_id != @service_id OR service_id IS NULL))
	BEGIN
		PRINT 'PAGINA NO INSERTADA'
		return
	END
	ELSE
	BEGIN
		-- si no existía se inserta normalmente
		insert into dbo.websites(user_id, url, service_id)
		values	(@user_id, @url, @service_id)
	END
END
GO
select * from dbo.services
select * from dbo.websites

execute dbo.new_website_from_service 2, 'https://youtube.com',1

-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR PÁGINA --------------------------
CREATE OR ALTER PROCEDURE dbo.update_website
(
	@website_id	INT,
	@url		VARCHAR(500),
	@affectedRows INT OUTPUT
)
AS
BEGIN
	IF EXISTS (SELECT 1 from dbo.websites where website_id = @website_id)
	BEGIN
		update dbo.websites
			set url = @url,
				reindex = 1,
				indexed = 0,
				index_date = NULL
		where website_id = @website_id
	END
	ELSE
	BEGIN
		raiserror('Pagina inexistente',16,1)
	END
	SELECT @affectedRows = @@ROWCOUNT;
END
GO
select * from dbo.websites
select * from dbo.services

declare @out int
execute insert_website 2, 'https://stackoverflow.com', @out output
go
-------------------------- PROCEDIMIENTO ALMACENADO ELIMINAR PAGINA --------------------------
CREATE OR ALTER PROCEDURE dbo.delete_website
(
	@website_id		INT,
	@affectedRows	INT OUTPUT
)
AS
BEGIN
	if exists(SELECT * from dbo.websites w where w.website_id = @website_id)
	BEGIN
		update w
			set isActive = 0,
				indexed = 0,
				reindex = 0,
				index_date = null
		from dbo.websites w
		where w.website_id = @website_id
	END
	ELSE
	BEGIN
		raiserror ('La operacion no se pudo realizar porque la pagina o el usuario no existen',16,1)
	END
	SELECT @affectedRows = @@ROWCOUNT;
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO PARA DESVINCULAR PAGINA DE SERVICIO --------------------------
CREATE OR ALTER PROCEDURE dbo.unlink_website_service
(
	@website_id		INT,
	@affectedRows	INT OUTPUT
)
AS
BEGIN
	if exists(SELECT * from dbo.websites w where w.website_id = @website_id)
	BEGIN
		update w
			set service_id = null
		from dbo.websites w
		where w.website_id = @website_id
	END
	ELSE
	BEGIN
		raiserror ('La operacion no se pudo realizar porque la pagina o el usuario no existen',16,1)
	END
	SELECT @affectedRows = @@ROWCOUNT;
END
GO

-------------------------- FUNCION PARA OBTENER EL DOMINIO DE UNA URL --------------------------
CREATE or ALTER FUNCTION dbo.get_domain (@url VARCHAR(500))
RETURNS VARCHAR(500)
AS BEGIN
    DECLARE @domain varchar(500)
	SET @domain =
		/* Get just the host name from a URL */
		SUBSTRING(@url,
			/* Starting Position (After any '//') */
			(CASE WHEN CHARINDEX('www.', @url) != 0 THEN (CHARINDEX('www.', @url) + 4)
				  WHEN CHARINDEX('//', @url) = 0 THEN 1 
			 ELSE CHARINDEX('//', @url) + 2 END),
			/* Length (ending on first '/' or on a '?') */
			CASE
				WHEN CHARINDEX('/', @url, CHARINDEX('//', @url) + 2) > 0 THEN CHARINDEX('/', @url, CHARINDEX('//', @url) + 2) - (CASE WHEN CHARINDEX('//', @url)= 0 THEN 1 ELSE CHARINDEX('//', @url) + 2 END)
				WHEN CHARINDEX('?', @url, CHARINDEX('//', @url) + 2) > 0 THEN CHARINDEX('?', @url, CHARINDEX('//', @url) + 2) - (CASE WHEN CHARINDEX('//', @url)= 0 THEN 1 ELSE CHARINDEX('//', @url) + 2 END)
				ELSE LEN(@url)
			END)
	return @domain
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO SETEAR PAGINA A REINDEXAR --------------------------
CREATE OR ALTER PROCEDURE dbo.reindex_website
(
	@website_id	INT,
	@affectedRows INT OUTPUT
)
AS
BEGIN
	IF EXISTS (SELECT 1 from dbo.websites where website_id = @website_id)
	BEGIN
		update dbo.websites
			set reindex = 1,
				indexed = 0,
				isUp = 1,
				index_date = NULL
		where website_id = @website_id
		AND isActive = 1
	END
	ELSE
	BEGIN
		raiserror('Pagina inexistente', 16,1)
	END
	SELECT @affectedRows = @@ROWCOUNT;
END
GO

-------------------------- PROCEDIMIENTO ALMACENADO SETEAR PAGINA COMO CAÍDA  --------------------------
CREATE or ALTER PROCEDURE dbo.set_website_down 
(
	@website_id int,
	@affectedRows INT OUTPUT
)
AS
BEGIN
	IF EXISTS (SELECT 1 from dbo.websites where website_id = @website_id)
    BEGIN
		update dbo.websites
		set isUp = 0,
			reindex = 0,
			indexed = 0
		where website_id = @website_id
	END
	ELSE
	BEGIN
		raiserror('Pagina inexistente',16,1)
	END
	SELECT @affectedRows = @@ROWCOUNT;
END
GO

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

-------------------------- PROCEDIMIENTO ALMACENADO SETEAR PAGINA COMO INDEXADA --------------------------
CREATE OR ALTER PROCEDURE dbo.set_website_indexed
(
	@website_id		INT,
	@affectedRows	INT OUTPUT
)
AS
BEGIN
	IF EXISTS (SELECT 1 FROM dbo.websites where website_id = @website_id)
	BEGIN
		update dbo.websites
		set indexed = 1,
			reindex = 0,
			index_date = CAST(GetDate() AS smalldatetime)
		where website_id = @website_id
	END
	ELSE
	BEGIN
		raiserror('Página inexistente', 16, 1)
	END
	SELECT @affectedRows = @@ROWCOUNT;
END
GO

-------------------------- TRIGGER PARA SETEAR SERVICIO INDEXADO SI TODAS SUS PAGINAS LO ESTAN --------------------------
CREATE OR ALTER TRIGGER tu_websites
ON dbo.websites
FOR update
AS
BEGIN
	update dbo.services
		set indexed = 1
	from dbo.services s
	join (SELECT
			service_id,
			COUNT(*) AS count,
			CASE WHEN SUM(CASE WHEN indexed = 0 AND isUp = 1 THEN 1 ELSE 0 END) > 0
					THEN 0 ELSE 1 END AS result
			FROM dbo.websites
			WHERE service_id IS NOT NULL
			GROUP BY service_id) as aux
	on s.service_id = aux.service_id
	where result = 1
END
GO

CREATE OR ALTER PROCEDURE dbo.get_service_website_indexed
(
	@url		VARCHAR(500),
	@service_id	INT
)
AS
BEGIN
	select * from dbo.websites
		where dbo.get_domain(url) = dbo.get_domain(@url)
		and service_id = @service_id
		and indexed = 1
END
GO

select * from dbo.websites

update dbo.websites
	set indexed = 1
	where website_id = 35

execute dbo.get_service_website_indexed 'https://github.com', 1

------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
select *
	from dbo.services s
	join (SELECT
			service_id,
			COUNT(*) AS count,
			CASE WHEN SUM(CASE WHEN indexed = 0 AND isUp = 1 THEN 1 ELSE 0 END) > 0
					THEN 0 ELSE 1 END AS result
			FROM dbo.websites
			WHERE service_id IS NOT NULL
			GROUP BY service_id) as aux
	on s.service_id = aux.service_id
	where result = 1
go

/*
DEPRECADO: SE USABA PARA ACTUALIZAR EL SERVICIO COMO INDEXADO = 0 SI ALGUNA DE LAS PAGINAS QUE SALIERON DE ESE SERVICIO SE PONIA A REINDEXAR
NO TIENE SENTIDO

	update dbo.services
		set indexed = 0
	from dbo.services s
	join (SELECT
			service_id,
			COUNT(*) AS count,
			CASE WHEN SUM(CASE WHEN indexed = 0 AND isUp = 1 THEN 1 ELSE 0 END) > 0
					THEN 0 ELSE 1 END AS result
			FROM dbo.websites
			WHERE service_id IS NOT NULL
			GROUP BY service_id) as aux
	on s.service_id = aux.service_id
	where result = 0
*/

select * from dbo.services

select * from dbo.websites
update dbo.websites
	set indexed = 1

select user_id, url
	from dbo.websites
	where reindex = 1
	group by user_id, url
	order by user_id


select user_id, STRING_AGG(CONCAT(url,' ',website_id) , ',') as pages, STRING_AGG(website_id, ',') as website_id
	from dbo.websites
	where reindex = 1 and isActive = 1
	group by user_id

/*

select user_id, STRING_AGG(url, ',') as pages, STRING_AGG(website_id, ',') as website_id
	from dbo.websites
	where reindex = 1 and isActive = 1
	group by user_id
*/

-- DOMINIO, www.youtube.com/*
-- AL PONER PARA REINDEXAR UNA PAGINA, SE TIENE QUE BORRAR TODO LO QUE EMPIECE CON EL DOMINIO DE ESA PAGINA


execute dbo.new_website 2, 'https://www.mercadolibre.com'
execute dbo.new_website 2, 'https://www.mercadolibre.com.ar'
execute dbo.new_website 2, 'https://www.mercadolibre.com.br'
execute dbo.new_website 2, 'https://www.mercadolibre.com.cl'
execute dbo.new_website 2, 'https://www.youtube.com/'
execute dbo.new_website 2, 'https://github.com/'
execute dbo.new_website 2, 'https://www.geeksforgeeks.org/'
execute dbo.new_website 2, 'https://www.mercadolibre.com'
execute dbo.new_website 2, 'https://www.mercadolibre.com'
go


select * from dbo.services
update dbo.services
	set indexed = 0,
		reindex = 1,
		index_date = null,
		isUp = 1

declare @out Int
execute dbo.insert_website 2, 'http://infobae.com', @out output

select * from dbo.services
DBCC CHECKIDENT ('websites', RESEED, 0);
