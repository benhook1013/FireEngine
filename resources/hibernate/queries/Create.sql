create
	table
		PLAYER_CHARACTER(
			PC_ID int unsigned not null AUTO_INCREMENT,
			PC_NAME varchar(40) not null,
			PC_PASSWORD varchar(40) not null,
			PC_PC_SETTINGS_ID int unsigned not null,
			PC_PC_COND_ID int unsigned not null,
			primary key(PC_ID)
		);

create
	table
		PC_SETTINGS(
			PC_SETTINGS_ID int unsigned not null AUTO_INCREMENT,
			PC_SETTINGS_MAP_EDITOR boolean not null,
			primary key(PC_SETTINGS_ID)
		);

create
	table
		PC_CONDITION(
			PC_COND_ID int unsigned not null AUTO_INCREMENT,
			PC_COND_CHAR_HEALTH_ID int unsigned not null,
			PC_COND_CHAR_MANA_ID int unsigned not null,
			PC_COND_CHAR_LEVEL_ID int unsigned not null,
			primary key(PC_COND_ID)
		);

create
	table
		CHAR_LEVEL(
			CHAR_LEVEL_ID int unsigned not null AUTO_INCREMENT,
			CHAR_LEVEL_LEVEL int unsigned not null,
			CHAR_LEVEL_EXPERIENCE int unsigned not null,
			primary key(CHAR_LEVEL_ID)
		);

create
	table
		CHAR_HEALTH(
			CHAR_HEALTH_ID int unsigned not null AUTO_INCREMENT,
			CHAR_HEALTH_HEALTH int unsigned not null,
			primary key(CHAR_HEALTH_ID)
		);

create
	table
		CHAR_MANA(
			CHAR_MANA_ID int unsigned not null AUTO_INCREMENT,
			CHAR_MANA_MANA int unsigned not null,
			primary key(CHAR_MANA_ID)
		);

create
	table
		GAMEMAP(
			MAP_ID int unsigned not null AUTO_INCREMENT,
			MAP_NAME varchar(100) not null,
			primary key(MAP_ID)
		);

insert
	into
		GAMEMAP
	values(
		null,
		"Mainland"
	);

create
	table
		BASE_ROOM(
			B_ROOM_ID int unsigned not null AUTO_INCREMENT,
			B_ROOM_MAP_ID int unsigned not null,
			B_ROOM_X int not null,
			B_ROOM_Y int not null,
			B_ROOM_NAME varchar(100) not null,
			B_ROOM_DESC varchar(2000) not null,
			B_ROOM_EXIT_N int unsigned default null,
			B_ROOM_EXIT_NE int unsigned default null,
			B_ROOM_EXIT_E int unsigned default null,
			B_ROOM_EXIT_SE int unsigned default null,
			B_ROOM_EXIT_S int unsigned default null,
			B_ROOM_EXIT_SW int unsigned default null,
			B_ROOM_EXIT_W int unsigned default null,
			B_ROOM_EXIT_NW int unsigned default null,
			primary key(B_ROOM_ID)
		);

insert
	into
		BASE_ROOM(
			B_ROOM_MAP_ID,
			B_ROOM_X,
			B_ROOM_Y,
			B_ROOM_NAME,
			B_ROOM_DESC
		)
	values(
		1,
		0,
		0,
		"The Lounge",
		"Around the location you see a comfortable setee, a cosy fire, and a darkwood bar 'manned' by a robotic server."
	);

create
	table
		BASE_ROOM_EXIT(
			B_ROOM_EXIT_ID int unsigned not null AUTO_INCREMENT,
			primary key(B_ROOM_EXIT_ID)
		);
