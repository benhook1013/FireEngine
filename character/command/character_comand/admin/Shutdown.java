package fireengine.character.command.character_comand.admin;

import java.util.ArrayList;
import java.util.Iterator;

import fireengine.character.Character;
import fireengine.character.command.character_comand.CommandCharacter;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exit.RoomExit;
import fireengine.gameworld.map.room.Room;
import fireengine.main.FireEngineMain;
import fireengine.util.StringUtils;

/*
 *    Copyright 2019 Ben Hook
 *    Shutdown.java
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

// TODO Make Admin flag on character and restrict this.
public class Shutdown extends CommandCharacter {

	public Shutdown() {
		super();
	}

	@Override
	public void doAction(Character character) {
		FireEngineMain.stop();
	}
}
