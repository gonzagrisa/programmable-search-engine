use users
drop table dbo.busquedas

CREATE TABLE dbo.busquedas
(
	busqueda_id		INT					IDENTITY,
	user_id			INT					NOT NULL,
	query			VARCHAR(500)		NOT NULL,
	date			DATE				NOT NULL,
	results			INT					NULL,
	sortBy			VARCHAR(12)			NULL,
	orderBy			VARCHAR(4)			NULL,
	tipoArchivo		VARCHAR(50)			NULL,
	terminos_relevantes	VARCHAR(200)	NULL,
	constraint PK__busquedas__END primary key (busqueda_id),
	constraint FK__busquedas__users__END foreign key (user_id) references dbo.users
);
go

CREATE TABLE dbo.palabras
(
	palabra_id	INT	IDENTITY,
	user_id		INT	NOT NULL,
	palabra		VARCHAR(100),
	constraint PK__palabras__END primary key (user_id, palabra_id),
	constraint FK__palabras__users__END foreign key (user_id) references dbo.users
);
go

------------------------ PROCEDIMIENTO PARA INSERTAR BUSQUEDA --------------------------
CREATE OR ALTER PROCEDURE dbo.insert_busqueda
(
	@user_id		INT,
	@query			VARCHAR(500),
	@results		INT,
	@sortyBy		VARCHAR(12)		NULL,
	@orderBy		VARCHAR(4)		NULL,
	@tipoArchivo	VARCHAR(50)		NULL,
	@terminos		VARCHAR(200)	NULL
)
AS
BEGIN
	insert into dbo.busquedas(user_id, query, date ,results, sortBy, orderBy, tipoArchivo, terminos_relevantes)
	values					 (@user_id, @query,  FORMAT(GetDate(), 'yyyy-MM-dd'), @results, @sortyBy, @orderBy, @tipoArchivo, @terminos)
END
GO

execute dbo.insert_busqueda 1, "pato detective", 5, null, null, null, null
execute dbo.insert_busqueda 1, "harry potter", 7, null, null, null, null
execute dbo.insert_busqueda 1, "formula 1", 1, null, null, null, null
execute dbo.insert_busqueda 1, "crepusculo", 0, null, null, null, null
execute dbo.insert_busqueda 1, "matrix", 3, null, null, null, null
go


------------------------ PROCEDIMIENTO PARA INSERTAR PALABRA --------------------------
CREATE OR ALTER PROCEDURE dbo.insert_palabra
(
	@user_id	INT,
	@palabra	VARCHAR(100)
)
AS
BEGIN
	insert into dbo.palabras(user_id, palabra)
	values				    (@user_id, @palabra)
END
GO

------------------------ PROCEDIMIENTO PARA OBTENER LAS BUSQUEDAS --------------------------
CREATE OR ALTER PROCEDURE dbo.get_busquedas
(
	@user_id	INT
)
AS
BEGIN
	IF (@user_id IS NULL)
	BEGIN
		select * from dbo.busquedas
		order by date desc
	END
	ELSE
	BEGIN
		select * from dbo.busquedas
			where user_id = @user_id
		order by date desc
	END
END
GO

execute dbo.get_busquedas null
go

------------------------ PROCEDIMIENTO PARA OBTENER EL TOP DE PALABRAS BUSCADAS --------------------------
CREATE OR ALTER PROCEDURE dbo.get_top_palabras
(
	@user_id	INT NULL
)
AS
BEGIN
	IF (@user_id IS NULL)
	BEGIN
		select top(10) palabra, count(*) as cantidad
			from dbo.palabras
		group by palabra
		order by count(*) desc
	END
	ELSE
	BEGIN
		select top(10) palabra, count(*) as cantidad
			from dbo.palabras
		where user_id = @user_id
		group by palabra
		order by count(*) desc
	END
END
GO

-- exec get_top_palabras @user_id = 1

------------------------ PROCEDIMIENTO PARA OBTENER CANTIDAD DE BUSQUEDAS CON RESULTADOS Y SIN RESULTADOS --------------------------
CREATE OR ALTER PROCEDURE dbo.get_resultados_busqueda
(
	@user_id	INT
)
AS
BEGIN
	IF (@user_id IS NOT NULL)
	BEGIN
		SELECT COUNT(CASE WHEN results = 0 THEN 1 END) sin_resultados, 
			   COUNT(CASE WHEN results > 0 THEN 1 END) con_resultados
		FROM dbo.busquedas
		where user_id = @user_id
	END
	ELSE
	BEGIN
		SELECT COUNT(CASE WHEN results = 0 THEN 1 END) sin_resultados, 
			   COUNT(CASE WHEN results > 0 THEN 1 END) con_resultados
		FROM dbo.busquedas
	END
END
GO

------------------------ PROCEDIMIENTO PARA OBTENER CANTIDAD DE BUSQUEDAS POR ORDEN --------------------------
CREATE OR ALTER PROCEDURE dbo.get_filtros_count
(
	@user_id	INT
)
AS
BEGIN
	SELECT	COUNT(CASE WHEN sortBy = 'popularity' THEN 1 END) popularidad,
			COUNT(CASE WHEN sortBy = 'date' THEN 1 END) fecha,
			COUNT(CASE WHEN orderBy = 'asc' THEN 1 END) ascendente,
			COUNT(CASE WHEN orderBy = 'desc' THEN 1 END) descendente
	FROM dbo.busquedas
	where user_id = @user_id
END
GO

------------------------ PROCEDIMIENTO PARA OBTENER CANTIDAD de BUSQUEDAS HECHAS POR --------------------------
CREATE OR ALTER PROCEDURE dbo.get_cantidades
(
	@user_id INT
)
AS
BEGIN
	SELECT	COUNT(CASE WHEN sortBy = 'popularity' THEN 1 END) popularidad,
			COUNT(CASE WHEN sortBy = 'date' THEN 1 END) fecha,
			COUNT(CASE WHEN orderBy = 'asc' THEN 1 END) ascendente,
			COUNT(CASE WHEN orderBy = 'desc' THEN 1 END) descendente,
			COUNT(CASE WHEN results = 0 THEN 1 END) sin_resultados,
			COUNT(CASE WHEN results > 0 THEN 1 END) con_resultados,
			COUNT(CASE WHEN date = CAST( GETDATE() AS Date ) THEN 1 END) realizadas_hoy,
			COUNT(*) totales
	FROM dbo.busquedas
	where  user_id = @user_id
END
GO

execute dbo.get_cantidades 1
go

------------------------ PROCEDIMIENTO PARA OBTENER CANTIDAD DE BUSQUEDAS POR TIPO DE ARCHIVO --------------------------
CREATE OR ALTER PROCEDURE dbo.get_tipo_count
(
	@user_id	INT
)
AS
BEGIN
	
	SELECT tipoArchivo, COUNT(*)
		FROM dbo.busquedas
	where user_id = @user_id
	group by tipoArchivo
END
GO


----------------------- TABLA CALENDARIO --------------------------------
drop table dbo.calendar
CREATE TABLE dbo.calendar(date date);

DECLARE @date date = '20200101';
WHILE @date <= '20211201'
BEGIN
    INSERT INTO dbo.calendar VALUES (@date);
    SET @date = FORMAT(DATEADD(day, 1, @date), 'yyyy-MM-dd');
END
GO

------------------------ PROCEDIMIENTO PARA BUSQUEDAS POR DIA --------------------------
CREATE OR ALTER PROCEDURE dbo.get_busquedas_dia
(
	@user_id	INT,
	@from		date NULL,
	@to			date NULL
)
AS
BEGIN
	IF (@to is NULL)
	BEGIN
		set @to = CAST( GETDATE() AS Date )
	END
	IF (@from is NULL)
	BEGIN
		-- set @from = DATEADD(DAY, -10, GETDATE())
		set @from = CAST( GETDATE() AS Date )
	END
	select c.date, count(query)
	from dbo.busquedas b
		right join calendar c
	on b.date = c.date
		where c.date >= @from
		and   c.date <= @to
		group by c.date
END
GO
