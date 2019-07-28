package fireengine.character;

import fireengine.character.character_class.CharacterClass;
import fireengine.character.command.CommandAction;
import fireengine.character.condition.Condition;
import fireengine.character.condition.ConditionPlayer;
import fireengine.character.player.Player;
import fireengine.character.player.exception.PlayerExceptionNullRoom;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.GameWorld;
import fireengine.gameworld.map.GameMap;
import fireengine.gameworld.map.room.Room;

/*
 *    Copyright 2019 Ben Hook
 *    Character.java
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    		http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/**
 * @author Ben Hook
 */
public abstract class Character {
	protected Character() {
	}

	public abstract int getId();

	protected abstract void setId(int id);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract CharacterClass getCharClass();

	public abstract void setCharClass(CharacterClass charClass);

	public abstract Condition getCondition();

	public abstract void setCondition(ConditionPlayer condition);

	public abstract Room getRoom();

	public abstract void setRoom(Room room) throws PlayerExceptionNullRoom;

	public abstract void acceptInput(String text);

	public abstract void acceptInput(CommandAction command);

	public abstract void sendToListeners(ClientConnectionOutput output);

	public abstract int getLevel();

	public abstract void setLevel(int level);

	public abstract int getCurrentHealth();

	public abstract int getMaxHealth();

	public abstract int getCurrentMana();

	public abstract int getMaxMana();

	public GameMap getMap() {
		Room room = getRoom();

		if (room != null) {
			return GameWorld.findMap(room.getMapId());
		} else {
			return null;
		}
	}

	public static boolean checkMapEditorPrivs(Character character) {
		if (character instanceof Player) {
			if (!((Player) character).getSettings().isMapEditor()) {
				character.sendToListeners(
						new ClientConnectionOutput("You don't have GameMap Editor privileges!", null, null));
				return false;
			} else {
				return true;
			}
		} else {
			character.sendToListeners(
					new ClientConnectionOutput("Only player characters can create exits, duh!", null, null));
			return false;
		}
	}
}
