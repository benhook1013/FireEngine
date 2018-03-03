SELECT
	PC_ID,
	PC_NAME,
	PC_PASSWORD,
	CHAR_LEVEL_LEVEL,
	CHAR_LEVEL_EXPERIENCE,
	CHAR_HEALTH_HEALTH,
	CHAR_MANA_MANA
FROM
	PLAYER_CHARACTER,
	PC_CONDITION,
	CHAR_LEVEL,
	CHAR_HEALTH,
	CHAR_MANA
WHERE
	PLAYER_CHARACTER.PC_PC_COND_ID = PC_CONDITION.PC_COND_ID
	AND PC_CONDITION.PC_COND_CHAR_LEVEL_ID = CHAR_LEVEL.CHAR_LEVEL_ID
	AND PC_CONDITION.PC_COND_CHAR_HEALTH_ID = CHAR_HEALTH.CHAR_HEALTH_ID
	AND PC_CONDITION.PC_COND_CHAR_MANA_ID = CHAR_MANA.CHAR_MANA_ID;