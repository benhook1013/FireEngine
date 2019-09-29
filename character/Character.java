package fireengine.character;

import java.util.Set;

import fireengine.character.character_class.CharacterClass;
import fireengine.character.condition.Condition;
import fireengine.character.condition.ConditionPlayer;
import fireengine.character.player.Player;
import fireengine.character.skillset.Skillset;
import fireengine.client_io.ClientConnectionOutput;
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

	public abstract String getName();

	public abstract void setName(String name);

	public abstract CharacterClass getCharClass();

	public abstract void setCharClass(CharacterClass charClass);

	public abstract Set<Skillset> getSkillsetList();

	public abstract void refreshSkillsetList();

	public abstract Condition getCondition();

	public abstract void setCondition(ConditionPlayer condition);

	public abstract Boolean isInWorld();

	/**
	 * If Character isInWorld, then return the room.
	 * 
	 * <p>
	 * Will return null if Character is not in world.
	 * </p>
	 * 
	 * @return
	 */
	public abstract Room getRoom();

	public abstract void setRoom(Room room);

	public abstract void acceptInput(String text);

	protected abstract void sendOutput(ClientConnectionOutput output);

	public abstract void sendToListeners(ClientConnectionOutput output);

	public abstract int getLevel();

	public abstract void setLevel(int level);

	public abstract int getCurrentHealth();

	public abstract int getMaxHealth();

	public abstract int getCurrentMana();

	public abstract int getMaxMana();

	/**
	 * If Character isInWorld, then return the GameMap.
	 * 
	 * <p>
	 * Will return null if Character is not in world.
	 * </p>
	 * 
	 * @return GameMap that Character is currently in.
	 */
	public GameMap getMap() {
		if (isInWorld()) {
			return getRoom().getMap();
		} else {
			return null;
		}
	}

	public static boolean checkMapEditorPrivs(Player character) {
		if (!((Player) character).getSettings().isMapEditor()) {
			character.sendToListeners(
					new ClientConnectionOutput("You don't have GameMap Editor privileges!", null, null));
			return false;
		} else {
			return true;
		}
	}

	public static boolean checkAdminPrivs(Player character) {
		if (!((Player) character).getSettings().isAdmin()) {
			character.sendToListeners(new ClientConnectionOutput("You don't have Admin privileges!", null, null));
			return false;
		} else {
			return true;
		}
	}
}
