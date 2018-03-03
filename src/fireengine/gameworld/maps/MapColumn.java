package fireengine.gameworld.maps;

import java.util.ArrayList;
import java.util.List;

import fireengine.gameworld.maps.Exceptions.Map_Exception_Exit_Exists;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Room_Exists;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Room_Null;
import fireengine.utils.CheckedHibernateException;

/*
 *    Copyright 2017 Ben Hook
 *    MapColumn.java
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
 * Represents a column in a map, contained by {@link GameMap}, and contains
 * {@link BaseRoom}s.
 * 
 * @author Ben Hook
 */
public class MapColumn {
	private String name;
	private List<BaseRoom> roomList;

	/**
	 * Initialises the array of rooms.
	 */
	private MapColumn() {
		roomList = new ArrayList<>((GameMap.MAP_DIMENSION * 2) + 1);
		for (int i = GameMap.MAP_DIMENSION; i > (((GameMap.MAP_DIMENSION * 2) + 1) * -1); i--) {
			roomList.add(null);
		}
	}

	protected MapColumn(String name) {
		this();
		this.name = name;
	}

	protected String getName() {
		return name;
	}

	@SuppressWarnings("unused")
	private void setName(String name) {
		this.name = name;
	}

	/**
	 * Converts the y coordinate into the room array index.
	 * 
	 * @param y
	 *            y coordinate to convert to array index
	 * @return
	 */
	private int yAdjust(int y) {
		y = (y * -1);
		y = (y + GameMap.MAP_DIMENSION);
		return y;
	}

	/**
	 * TODO Consider if this is needed.
	 * 
	 * Returns list of rooms for the {@link MapColumn}.
	 * 
	 * @return
	 */
	protected List<BaseRoom> getRooms() {
		return roomList;
	}

	/**
	 * Returns the room (or null) at the specified y coordinate, throwing an
	 * exception if coordinate is out of bounds.
	 * 
	 * @param y
	 * @return
	 * @throws Map_Exception_Out_Of_Bounds
	 */
	protected BaseRoom getRoom(int y) throws Map_Exception_Out_Of_Bounds {
		GameMap.checkRoomCoordinate(y);
		y = yAdjust(y);
		return roomList.get(y);
	}

	/**
	 * Inserts the given room into the room list at the specified y coordinate,
	 * throwing exception on out of bounds or if room already in that coordinate.
	 * 
	 * @param y
	 * @param room
	 * @throws Map_Exception_Out_Of_Bounds
	 * @throws Map_Exception_Room_Exists
	 */
	protected void setRoom(int y, BaseRoom room) throws Map_Exception_Out_Of_Bounds, Map_Exception_Room_Exists {
		synchronized (roomList) {
			GameMap.checkRoomCoordinate(y);
			y = yAdjust(y);

			BaseRoom foundRoom = roomList.get(y);

			if (foundRoom != null) {
				throw new Map_Exception_Room_Exists("MapColumn: setRoom found room already at designated coordinates.");
			} else {
				roomList.set(y, room);
			}
		}
	}

	/**
	 * Attempts to remove room at given y coordinate from room list and also deletes
	 * room from database. Throws exception if coordinate is out of bounds, room at
	 * coordinate is null, the room still has exits, or upon Hibernate problem.
	 * 
	 * @param y
	 * @throws Map_Exception_Out_Of_Bounds
	 * @throws Map_Exception_Room_Null
	 * @throws Map_Exception_Exit_Exists
	 * @throws CheckedHibernateException
	 */
	protected void deleteRoom(int y) throws Map_Exception_Out_Of_Bounds, Map_Exception_Room_Null,
			Map_Exception_Exit_Exists, CheckedHibernateException {
		synchronized (roomList) {
			GameMap.checkRoomCoordinate(y);
			y = yAdjust(y);
			BaseRoom foundRoom = roomList.get(y);

			if (foundRoom != null) {
				BaseRoom.deleteRoom(foundRoom);
				roomList.set(y, null);
			} else {
				throw new Map_Exception_Room_Null("MapColumn: Tried to deleteRoom on a null room");
			}
		}
	}
}
