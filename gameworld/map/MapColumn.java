package fireengine.gameworld.map;

import java.util.ArrayList;
import java.util.List;

import fireengine.gameworld.map.exception.MapExceptionExitExists;
import fireengine.gameworld.map.exception.MapExceptionOutOfBounds;
import fireengine.gameworld.map.exception.MapExceptionRoomExists;
import fireengine.gameworld.map.exception.MapExceptionRoomNull;
import fireengine.gameworld.map.room.Room;
import fireengine.util.CheckedHibernateException;

/*
 *    Copyright 2019 Ben Hook
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
 * {@link Room}s.
 *
 * @author Ben Hook
 */
public class MapColumn {
	private String name;
	private List<Room> roomList;

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
	 * @param y y coordinate to convert to array index
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
	 * Returns list of {@link Room}s for the {@link MapColumn}.
	 *
	 * @return
	 */
	protected List<Room> getRooms() {
		return roomList;
	}

	/**
	 * Returns the {@link Room} (or null) at the specified y coordinate,
	 * throwing an exception if coordinate is out of bounds.
	 *
	 * @param y
	 * @return
	 * @throws MapExceptionOutOfBounds
	 */
	protected Room getRoom(int y) throws MapExceptionOutOfBounds {
		GameMap.checkRoomCoordinate(y);
		y = yAdjust(y);
		return roomList.get(y);
	}

	/**
	 * Inserts the given {@link Room} into the room list at the specified y
	 * coordinate, throwing exception on out of bounds or if room already in that
	 * coordinate.
	 *
	 * @param y
	 * @param room
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionRoomExists
	 */
	protected void setRoom(int y, Room room) throws MapExceptionOutOfBounds, MapExceptionRoomExists {
		synchronized (roomList) {
			GameMap.checkRoomCoordinate(y);
			y = yAdjust(y);

			Room foundRoom = roomList.get(y);

			if (foundRoom != null) {
				throw new MapExceptionRoomExists("MapColumn: setRoom found room already at designated coordinates.");
			} else {
				roomList.set(y, room);
			}
		}
	}

	/**
	 * Attempts to remove {@link Room} at given y coordinate from room list and
	 * also deletes room from database. Throws exception if coordinate is out of
	 * bounds, room at coordinate is null, the room still has exits, or upon
	 * Hibernate problem.
	 *
	 * @param y
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionRoomNull
	 * @throws MapExceptionExitExists
	 * @throws CheckedHibernateException
	 */
	protected void deleteRoom(int y)
			throws MapExceptionOutOfBounds, MapExceptionRoomNull, MapExceptionExitExists, CheckedHibernateException {
		synchronized (roomList) {
			GameMap.checkRoomCoordinate(y);
			y = yAdjust(y);
			Room foundRoom = roomList.get(y);

			if (foundRoom != null) {
				Room.deleteRoom(foundRoom);
				roomList.set(y, null);
			} else {
				throw new MapExceptionRoomNull("MapColumn: Tried to deleteRoom on a null room");
			}
		}
	}
}