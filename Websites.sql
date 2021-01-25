use users

-- ################## TABLA P�GINAS ##################
CREATE TABLE dbo.websites
(
    user_id	INT				NOT NULL,
    url		VARCHAR(500)	NOT NULL,
	reindex	BIT				NOT NULL default 1,
	constraint PK__websites__END primary key (user_id, url),
	constraint CK__websites__reindex__END check (reindex in (0, 1)),
	constraint FK__websites__users__END foreign key (user_id) references dbo.users on delete cascade
);
go

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

CREATE or ALTER PROCEDURE dbo.get_websites
(
	@user_id	INT
)
as
begin
	select * from dbo.websites w
		where w.user_id = @user_id
end
go

execute dbo.get_websites 1
go

-------------------------- PROCEDIMIENTO ALMACENADO INSERTAR NUEVA PAGINA --------------------------
CREATE OR ALTER PROCEDURE dbo.new_website
(
	@user_id	INT,
	@url		VARCHAR(500)
)
as
begin
	if exists(SELECT 1 
				from dbo.websites w
				where w.user_id = @user_id
				AND dbo.get_domain(w.url) = dbo.get_domain(@url))
	BEGIN
		raiserror ('El Dominio ya se encuentra registrado',16,1)
		return
	END
	ELSE
	BEGIN
		insert into dbo.websites(user_id, url)
		values(@user_id, @url)
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
