package fireengine.gameworld.maps;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import fireengine.characters.commands.character_commands.general.Map;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.Gameworld;
import fireengine.gameworld.maps.exceptions.MapExceptionDirectionNotSupported;
import fireengine.gameworld.maps.exceptions.MapExceptionExitExists;
import fireengine.gameworld.maps.exceptions.MapExceptionExitRoomNull;
import fireengine.gameworld.maps.exceptions.MapExceptionOutOfBounds;
import fireengine.gameworld.maps.exceptions.MapExceptionRoomExists;
import fireengine.gameworld.maps.exceptions.MapExceptionRoomNull;
import fireengine.main.FireEngineMain;
import fireengine.utils.CheckedHibernateException;
import fireengine.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    GameMap.java
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
 * Contains indirectly all {@link BaseRoom}s, within {@link MapColumn}s.
 * Contains functions to do with the map, including generating {@link Map}
 * command display.
 * 
 * @author Ben Hook
 */
@Entity
@Table(name = "GAME_MAP")
public class GameMap {
	// From centre point 0,0 the most far away room will be
	// MAP_DIMENSION,MAP_DIMENSION
	/**
	 * From centre point (0,0) the most far away room will be
	 * (MAP_DIMENSION,MAP_DIMENSION).
	 */
	@Transient
	public static final int MAP_DIMENSION = 10;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "GAME_MAP_ID")
	private int id;
	@Column(name = "GAME_MAP_NAME")
	private String name;

	@Transient
	private List<MapColumn> columnList;

	/**
	 * Initialises the column list and instantiates the column objects.
	 */
	private GameMap() {
		columnList = new ArrayList<>((MAP_DIMENSION * 2) + 1);
		for (int i = (MAP_DIMENSION * -1); i < (MAP_DIMENSION + 1); i++) {
			MapColumn column = new MapColumn(name + "_column_" + i);
			columnList.add(column);
		}
	}

	public GameMap(String name) {
		this();
		this.name = name;
	}

	public int getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Converts the s coordinate into the column array index.
	 * 
	 * @param x
	 *            x coordinate to convert into column array index
	 * @return
	 */
	private int xAdjust(int x) {
		x = (x + MAP_DIMENSION);
		return x;
	}

	private MapColumn getColumn(int x) throws MapExceptionOutOfBounds {
		checkRoomCoordinate(x);
		x = xAdjust(x);
		return columnList.get(x);
	}

	/**
	 * Returns the room (or null) at specified coordinates, throwing an exception if
	 * coordinates are out of bounds.
	 * 
	 * @param x
	 *            x coordinate of room to find
	 * @param y
	 *            y coordinate of room to find
	 * @return
	 * @throws MapExceptionOutOfBounds
	 */
	public BaseRoom getRoom(int x, int y) throws MapExceptionOutOfBounds {
		MapColumn column = getColumn(x);
		if (column == null) {
			MyLogger.log(Level.WARNING, "GameMap: No MapColumn found for getRoom with x: '" + x + "'.");
			return null;
		} else {
			return column.getRoom(y);
		}
	}

	/**
	 * Attempts to get room in the direction off of the given room. Exception occurs
	 * if the resulting coordinate would be out of bounds, or given direction is not
	 * yet supported by used functions.
	 * 
	 * @param room
	 *            origin room of which the direction points
	 * @param direction
	 *            direction from origin room the sought after room is
	 * @return
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionDirectionNotSupported
	 */
	public BaseRoom getRoom(BaseRoom room, Directions.DIRECTION direction)
			throws MapExceptionOutOfBounds, MapExceptionDirectionNotSupported {
		int otherX = xAdjustDirection(room.getX(), direction);
		int othery = yAdjustDirection(room.getY(), direction);
		return getRoom(otherX, othery);
	}

	/**
	 * Inserts the given room into the room list at the specified coordinates,
	 * throwing exception on out of bounds or if room already in that coordinate.
	 * 
	 * @param x
	 * @param y
	 * @param room
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionRoomExists
	 */
	public void setRoom(int x, int y, BaseRoom room) throws MapExceptionOutOfBounds, MapExceptionRoomExists {
		synchronized (columnList) {
			MapColumn column = getColumn(x);

			if (column != null) {
				try {
					column.setRoom(y, room);
				} catch (MapExceptionRoomExists e) {
					throw new MapExceptionRoomExists(
							"GameMap: setRoom found room already at " + room.getCoords() + ".", e);
				}
			} else {
				MyLogger.log(Level.SEVERE, "GameMap: Tried to setRoom on a null column");
				return;
			}
		}
	}

	/**
	 * 
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionRoomExists
	 * @throws CheckedHibernateException
	 */
	public void createRoom(int x, int y)
			throws MapExceptionOutOfBounds, MapExceptionRoomExists, CheckedHibernateException {
		synchronized (columnList) {
			checkRoomCoordinate(x);
			checkRoomCoordinate(y);

			BaseRoom foundRoom = getRoom(x, y);
			if (foundRoom != null) {
				throw new MapExceptionRoomExists(
						"GameMap: createRoom found room already at designated coordinates.");
			}

			BaseRoom newRoom;
			try {
				newRoom = BaseRoom.createRoom(id, x, y);
			} catch (MapExceptionRoomNull e) {
				MyLogger.log(Level.SEVERE,
						"GameMap: Weird error, MapExceptionRoomNull while trying to BaseRoom.createRoom.", e);
				return;
			}
			try {
				setRoom(x, y, newRoom);
			} catch (MapExceptionRoomExists e) {
				try {
					BaseRoom.deleteRoom(newRoom);
				} catch (MapExceptionRoomNull e2) {
					MyLogger.log(Level.SEVERE, "GameMap: Weird error, MapExceptionRoomNull while trying to setRoom.",
							e2);
					return;
				} catch (MapExceptionExitExists e3) {
					MyLogger.log(Level.WARNING,
							"GameMap: MapExceptionExitExists while deleteRoom after MapExceptionRoomExists on createRoom.",
							e3);
					return;
				}
				MyLogger.log(Level.WARNING,
						"GameMap: createRoom found room already at designated coordinates after creation, cleaning up.",
						e);
				throw new MapExceptionRoomExists(
						"GameMap: createRoom found room already at designated coordinates after creation, cleaning up.",
						e);
			}
		}
	}

	/**
	 * Attempts to create a room in the specified direction, off of the given room.
	 * 
	 * @param baseRoom
	 * @param direction
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionRoomExists
	 * @throws CheckedHibernateException
	 * @throws MapExceptionDirectionNotSupported
	 */
	public void createRoom(BaseRoom baseRoom, Directions.DIRECTION direction) throws MapExceptionOutOfBounds,
			MapExceptionRoomExists, CheckedHibernateException, MapExceptionDirectionNotSupported {
		createRoom(xAdjustDirection(baseRoom.getX(), direction), yAdjustDirection(baseRoom.getY(), direction));
	}

	/**
	 * Attempts to remove all exits of room, remove room from map and delete room
	 * from database.
	 * 
	 * @param x
	 * @param y
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 */
	public void destroyRoom(int x, int y)
			throws MapExceptionOutOfBounds, MapExceptionRoomNull, CheckedHibernateException {
		synchronized (columnList) {
			MapColumn column = getColumn(x);

			if (column != null) {
				BaseRoom room = column.getRoom(y);

				for (Directions.DIRECTION direction : Directions.DIRECTION.values()) {
					try {
						destroyExit(room, direction);
					} catch (MapExceptionOutOfBounds e) {
						// This is expected in some situations and is allowed
					} catch (MapExceptionExitRoomNull e) {
						// This is expected in some situations and is allowed
					} catch (MapExceptionDirectionNotSupported e) {
						MyLogger.log(Level.WARNING, "GameMap: MapExceptionDirectionNotSupported while destroyRoom.",
								e);
						return;
					}
				}

				try {
					column.deleteRoom(y);
				} catch (MapExceptionExitExists e) {
					MyLogger.log(Level.WARNING,
							"GameMap: MapExceptionExitExists while deleteRoom, but all exits should have already been destroyed.",
							e);
					return;
				}
			} else {
				MyLogger.log(Level.SEVERE, "GameMap: Tried to deleteRoom on a null column.");
				return;
			}
		}
	}

	/**
	 * Attempts to destroy the room in the specified direction, off of the given
	 * room.
	 * 
	 * @param baseRoom
	 * @param direction
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 * @throws MapExceptionDirectionNotSupported
	 */
	public void destroyRoom(BaseRoom baseRoom, Directions.DIRECTION direction) throws MapExceptionOutOfBounds,
			MapExceptionRoomNull, CheckedHibernateException, MapExceptionDirectionNotSupported {
		System.out.println("Destroying (" + xAdjustDirection(baseRoom.getX(), direction) + ","
				+ yAdjustDirection(baseRoom.getY(), direction) + ").");
		destroyRoom(xAdjustDirection(baseRoom.getX(), direction), yAdjustDirection(baseRoom.getY(), direction));
	}

	/**
	 * Creates an exit in direction specified, from room supplied.
	 * 
	 * @param room
	 * @param direction
	 * @throws MapExceptionRoomNull
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionExitRoomNull
	 * @throws MapExceptionExitExists
	 * @throws MapExceptionDirectionNotSupported
	 * @throws CheckedHibernateException
	 */
	public void createExit(BaseRoom room, Directions.DIRECTION direction)
			throws MapExceptionRoomNull, MapExceptionOutOfBounds, MapExceptionExitRoomNull,
			MapExceptionExitExists, MapExceptionDirectionNotSupported, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("GameMap: Tried to set exit on null room.");
		}
		if (room.getExit(direction) != null) {
			throw new MapExceptionExitExists(
					"GameMap: Exit is not null " + direction.toString() + " of " + room.getRoomName() + ".");
		}

		BaseRoom otherRoom = getRoom(room, direction);
		if (otherRoom == null) {
			throw new MapExceptionExitRoomNull("GameMap: Tried to set exit on null adjacent room.");
		}
		if (room.getExit(Directions.oppositeDirection(direction)) != null) {
			throw new MapExceptionExitExists("GameMap: Exit is not null "
					+ Directions.oppositeDirection(direction).toString() + " of " + otherRoom.getRoomName());
		}

		BaseRoomExit newExit = new BaseRoomExit();
		room.setExit(direction, newExit);
		BaseRoom.saveRoom(room);
		otherRoom.setExit(Directions.oppositeDirection(direction), newExit);
		BaseRoom.saveRoom(otherRoom);
	}

	/**
	 * Removes exit in specified direction, from room given.
	 * 
	 * @param room
	 * @param direction
	 * @throws MapExceptionRoomNull
	 * @throws MapExceptionOutOfBounds
	 * @throws MapExceptionExitRoomNull
	 * @throws MapExceptionDirectionNotSupported
	 * @throws CheckedHibernateException
	 */
	public void destroyExit(BaseRoom room, Directions.DIRECTION direction)
			throws MapExceptionRoomNull, MapExceptionOutOfBounds, MapExceptionExitRoomNull,
			MapExceptionDirectionNotSupported, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("GameMap: Tried to remove exit on null room.");
		}
		BaseRoomExit roomExit = room.getExit(direction);

		BaseRoom otherRoom = getRoom(room, direction);
		if (otherRoom == null) {
			throw new MapExceptionExitRoomNull("GameMap: Tried to remove exit to a null room.");
		}
		BaseRoomExit otherRoomExit = otherRoom.getExit(Directions.oppositeDirection(direction));

		if ((roomExit != null) && (otherRoomExit != null)) {
			if (roomExit.getId() == otherRoomExit.getId()) {
				BaseRoom.deleteExit(roomExit);
			} else {
				BaseRoom.deleteExit(roomExit);
				BaseRoom.deleteExit(otherRoomExit);
			}
		} else {
			if (roomExit != null) {
				BaseRoom.deleteExit(roomExit);
			}
			if (otherRoomExit != null) {
				BaseRoom.deleteExit(otherRoomExit);
			}
		}
		room.setExit(direction, null);
		BaseRoom.saveRoom(room);
		otherRoom.setExit(Directions.oppositeDirection(direction), null);
		BaseRoom.saveRoom(otherRoom);
	}

	/**
	 * Creates a new, blank map with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws CheckedHibernateException
	 */
	public static GameMap createMap(String name) throws CheckedHibernateException {
		GameMap newMap = new GameMap(name);

		saveMap(newMap);
		return newMap;
	}

	/**
	 * Persists the provided map into the database.
	 * 
	 * @param gameMap
	 * @throws CheckedHibernateException
	 */
	public static void saveMap(GameMap gameMap) throws CheckedHibernateException {
		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			hibSess.saveOrUpdate(gameMap);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("GameMap: Hibernate error while trying to saveMap.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}

	/**
	 * Generates an output object with a visual display of the MAP surrounding the
	 * given room.
	 * 
	 * @param output
	 *            Existing output object to append to, if any
	 * @param baseRoom
	 *            Room around which to display map
	 * @param size
	 *            Number of rooms in each direction to display
	 * @return
	 */
	public static ClientConnectionOutput displayMap(ClientConnectionOutput output, BaseRoom baseRoom, int size) {
		GameMap gameMap = Gameworld.findMap(baseRoom.getMapId());

		if (output == null) {
			output = new ClientConnectionOutput();
		}

		output.newLine();
		output.addPart("Map around \"" + baseRoom.getRoomName() + "\" " + baseRoom.getCoords(), null, null);

		class Line_Builder {
			// public String getBorder(int size) {
			// String border = "";
			//
			// for (int i = 0; i < size; i++) {
			// border = border + "-";
			// }
			//
			// return border;
			// }

			public ClientConnectionOutput buildLines(ClientConnectionOutput output, int lineX, int lineY,
					int lineSize) {
				String lineTop = "";
				String lineMid = "";
				String lineBot = "";

				for (int i = lineX; i < (lineX + lineSize); i++) {
					BaseRoom foundRoom;
					try {
						foundRoom = gameMap.getRoom(i, lineY);
					} catch (MapExceptionOutOfBounds e) {
						foundRoom = null;
					}

					if (foundRoom == null) {
						lineTop = lineTop + "     ";
						lineMid = lineMid + "  *  ";
						lineBot = lineBot + "     ";
					} else {
						String nw = " ";
						String n = " ";
						String ne = " ";
						String w = " ";
						String center = " ";
						String e = " ";
						String sw = " ";
						String s = " ";
						String se = " ";

						try {
							if (foundRoom.getExit(Directions.DIRECTION.NORTHWEST) != null) {
								nw = "\\";
							}

							if (foundRoom.getExit(Directions.DIRECTION.NORTH) != null) {
								n = "|";
							}
							if (foundRoom.getExit(Directions.DIRECTION.NORTHEAST) != null) {
								ne = "/";
							}
							if (foundRoom.getExit(Directions.DIRECTION.WEST) != null) {
								w = "-";
							}
							if (foundRoom == baseRoom) {
								center = "x";
							} else if (!foundRoom.getPCs().isEmpty()) {
								center = "o";
							}
							if (foundRoom.getExit(Directions.DIRECTION.EAST) != null) {
								e = "-";
							}
							if (foundRoom.getExit(Directions.DIRECTION.SOUTHWEST) != null) {
								sw = "/";
							}
							if (foundRoom.getExit(Directions.DIRECTION.SOUTH) != null) {
								s = "|";
							}
							if (foundRoom.getExit(Directions.DIRECTION.SOUTHEAST) != null) {
								se = "\\";
							}
						} catch (MapExceptionDirectionNotSupported e2) {
							MyLogger.log(Level.WARNING,
									"GameMap: MapExceptionDirectionNotSupported while displayMap.", e2);
						}

						lineTop = lineTop + nw + " " + n + " " + ne;
						lineMid = lineMid + w + "[" + center + "]" + e;
						lineBot = lineBot + sw + " " + s + " " + se;
					}
				}

				output.newLine();
				output.addPart(lineTop, null, null);
				output.newLine();
				output.addPart(lineMid, null, null);
				output.newLine();
				output.addPart(lineBot, null, null);

				return output;
			}
		}
		;
		Line_Builder lineBuilder = new Line_Builder();

		for (int i = (baseRoom.getY() + size); i > (baseRoom.getY() - size - 1); i--) {
			output = lineBuilder.buildLines(output, (baseRoom.getX() - size), i, ((size * 2) + 1));
		}
		output.newLine();
		output.addPart(size + "x" + size + " map", null, null);

		return output;
	}

	/**
	 * Checks given coordinate is within map dimensions.
	 * 
	 * @param coord
	 * @throws MapExceptionOutOfBounds
	 */
	public static void checkRoomCoordinate(int coord) throws MapExceptionOutOfBounds {
		if (((GameMap.MAP_DIMENSION * -1) > coord) || (coord > GameMap.MAP_DIMENSION)) {
			throw new MapExceptionOutOfBounds("GameMap: Failed coordinate check with coord: '" + coord + "'");
		}
	}

	/**
	 * Returns x coordinate that occupies the given direction from the supplied
	 * coordinate.
	 * 
	 * @param x
	 * @param direction
	 * @return
	 * @throws MapExceptionDirectionNotSupported
	 */
	public static int xAdjustDirection(int x, Directions.DIRECTION direction)
			throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case NORTHWEST: {
			return (x - 1);
		}
		case NORTH: {
			return (x + 0);
		}
		case NORTHEAST: {
			return (x + 1);
		}
		case EAST: {
			return (x + 1);
		}
		case SOUTHEAST: {
			return (x + 1);
		}
		case SOUTH: {
			return (x + 0);
		}
		case SOUTHWEST: {
			return (x - 1);
		}
		case WEST: {
			return (x - 1);
		}
		default: {
			MyLogger.log(Level.WARNING,
					"GameMap: Failed to xAdjustDirection for direction: " + direction.toString() + ".");
			throw new MapExceptionDirectionNotSupported(
					"GameMap: xAdjustDirection missing case for direction " + direction.toString() + ".");
		}
		}
	}

	/**
	 * Returns y coordinate that occupies the given direction from the supplied
	 * coordinate.
	 * 
	 * @param y
	 * @param direction
	 * @return
	 * @throws MapExceptionDirectionNotSupported
	 */
	public static int yAdjustDirection(int y, Directions.DIRECTION direction)
			throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case NORTHWEST: {
			return (y + 1);
		}
		case NORTH: {
			return (y + 1);
		}
		case NORTHEAST: {
			return (y + 1);
		}
		case EAST: {
			return (y + 0);
		}
		case SOUTHEAST: {
			return (y - 1);
		}
		case SOUTH: {
			return (y - 1);
		}
		case SOUTHWEST: {
			return (y - 1);
		}
		case WEST: {
			return (y + 0);
		}
		default: {
			MyLogger.log(Level.WARNING,
					"GameMap: Failed to yAdjustDirection for direction: " + direction.toString() + ".");
			throw new MapExceptionDirectionNotSupported(
					"GameMap: yAdjustDirection missing case for direction " + direction.toString() + ".");
		}
		}
	}
}
