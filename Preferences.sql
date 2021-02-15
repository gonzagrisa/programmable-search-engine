use users
drop table dbo.preferences
-- ################## TABLA PREFERENCIAS ##################
CREATE TABLE dbo.preferences
(
	user_id			INT				NOT NULL,
	border_width	DECIMAL (3, 2)  NOT NULL DEFAULT 0.1,
	border_radius	TINYINT			NOT NULL DEFAULT 20,
	icon_url		VARCHAR(500)	NOT NULL DEFAULT 'https://www.shareicon.net/data/256x256/2016/11/22/854956_search_512x512.png',
	icon_size		TINYINT			NOT NULL DEFAULT 30,
	placeholder		VARCHAR(100)	NOT NULL DEFAULT 'Buscar',
	color			VARCHAR(7)		NOT NULL DEFAULT '#D3D3D3',
	constraint PK__preferences__END primary key (user_id),
	constraint FK__preferences__users__END foreign key (user_id) references dbo.users
);
go


-------------------------- TRIGGER PARA INSERTAR LAS OPCIONES POR DEFECTO AL NUEVO USUARIO CREADO --------------------------
CREATE OR ALTER trigger ti_users
on dbo.users
for insert
as
begin
	declare @id INT
	set @id = (select user_id from inserted)
	insert into dbo.preferences(user_id)
	values (@id)
end
go


-------------------------- PROCEDIMIENTO ALMACENADO OBTENER PREFERENCIAS USUARIO --------------------------
create or alter procedure dbo.get_preferences
(
	@user_id	INT
)
as
begin
	select * from dbo.preferences p
		where p.user_id = @user_id
end
go

execute dbo.get_preferences 2

-------------------------- PROCEDIMIENTO ALMACENADO ACTUALIZAR PREFERENCIAS BUSCADOR --------------------------
create or alter procedure dbo.update_preferences
(
	@user_id		INT,
	@border_width	DECIMAL (3, 2),
	@border_radius	TINYINT,
	@icon_url		VARCHAR(500),
	@icon_size		TINYINT,
	@placehoder		VARCHAR(100),
	@color			VARCHAR(7)
)
as
begin
	update p
		set p.color = @color,
			p.border_width = @border_width,
			p.border_radius = @border_radius,
			p.icon_url = @icon_url,
			p.icon_size = @icon_size,
			p.placeholder = @placehoder
		from dbo.preferences p
		where p.user_id = @user_id
end
go




-- CURSOR EJEMPLO
DECLARE	@user_id INT

DECLARE cursor_user CURSOR
FOR SELECT user_id
    FROM 
        dbo.users
OPEN cursor_user
FETCH NEXT FROM cursor_user INTO 
    @user_id

WHILE @@FETCH_STATUS = 0
    BEGIN
        insert into dbo.preferences(user_id)
		values (@user_id)
        FETCH NEXT FROM cursor_user INTO 
            @user_id 
    END;

CLOSE cursor_user;
DEALLOCATE cursor_user;
