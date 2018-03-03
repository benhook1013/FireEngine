package fireengine.characters;

import fireengine.characters.commands.Action_Command;
import fireengine.characters.player.Player_Character;
import fireengine.characters.player.exceptions.PC_Exception_Null_Room;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.Gameworld;
import fireengine.gameworld.maps.BaseRoom;
import fireengine.gameworld.maps.GameMap;

/*
 *    Copyright 2017 Ben Hook
 *    Base_Character.java
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

public abstract class Base_Character {
	protected Base_Character() {
	}

	public abstract int getId();

	protected abstract void setId(int id);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getPassword();

	public abstract void setPassword(String password);

	public abstract BaseRoom getRoom();

	public abstract void setRoom(BaseRoom room) throws PC_Exception_Null_Room;

	public abstract void acceptInput(String text);

	public abstract void acceptInput(Action_Command command);

	public abstract void sendToListeners(ClientConnectionOutput output);

	public abstract int getLevel();

	public abstract int getCurrentHealth();

	public abstract int getMaxHealth();

	public abstract int getCurrentMana();

	public abstract int getMaxMana();

	public GameMap getMap() {
		BaseRoom room = getRoom();

		if (room != null) {
			return Gameworld.findMap(room.getMapId());
		} else {
			return null;
		}
	}

	public static boolean checkMapEditorPrivs(Base_Character character) {
		if (character instanceof Player_Character) {
			if (!((Player_Character) character).getSettings().isMapEditor()) {
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
