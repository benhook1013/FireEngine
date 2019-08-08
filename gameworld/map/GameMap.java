package fireengine.gameworld.map;

import java.util.List;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exception.MapExceptionExitExists;
import fireengine.gameworld.map.exception.MapExceptionExitRoomNull;
import fireengine.gameworld.map.exception.MapExceptionOutOfBounds;
import fireengine.gameworld.map.exception.MapExceptionRoomExists;
import fireengine.gameworld.map.exception.MapExceptionRoomNull;
import fireengine.gameworld.map.exit.RoomExit;
import fireengine.gameworld.map.room.Room;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
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
 * Contains all {@link Room}s. Contains functions to do with the map, including
 * function to display map.
 *
 * @author Ben Hook
 */
@Entity
@Table(name = "GAME_MAP")
public class GameMap {
	/**
	 * From centre point (0, 0, 0) the most far away room will be (MAP_DIMENSION,
	 * MAP_DIMENSION, MAP_DIMENSION).
	 */
	@Transient
	public static final int MAP_DIMENSION = 10;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "GAME_MAP_ID")
	@NotNull
	private int id;

	@Column(name = "GAME_MAP_NAME")
	@NotNull
	private String name;

	// There is some acknowledged danger here in that the room array and database
	// can get out of synch. Must be careful for all room creation/modification type
	// methods to be aware.
	// Was chosen over having persistable entities such as zDimension (containing
	// yDimensions) and yDimensions as they would require separate classes and
	// tables.
	@Transient
	private Room[][][] roomArray; // Room[z][y][x]

//	@OneToOne(fetch = FetchType.EAGER)
//	@Cascade(CascadeType.ALL)
//	@JoinColumn(name = "PLAYER_PLAYER_SETTINGS_ID")
//	@NotNull
//	private PlayerSetting settings;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "GAME_MAP_SPAWN_ROOM_ID")
	private Room spawnRoom;

	/**
	 * Initialises the 3D Room array.
	 */
	private GameMap() {
		// TODO Modify to use granular map dimension settings
		// TODO Save map dimensions to map
		roomArray = new Room[MAP_DIMENSION][MAP_DIMENSION][MAP_DIMENSION];
	}

	public GameMap(String name) {
		this();
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Room getSpawnRoom() {
		return this.spawnRoom;
	}

	public Room getSpawnRoomOrCentre() {
		Room returnRoom = this.spawnRoom;
		if (returnRoom == null) {
			try {
				returnRoom = getRoom(0, 0, 0);
			} catch (MapExceptionOutOfBounds e) {
				MyLogger.log(Level.WARNING,
						"GameMap: MapExceptionOutOfBounds during getSpawnRoomOrCentre when trying to get centre room 0,0,0.",
						e);
			}
		}

		return returnRoom;
	}

	public void setSpawnRoom(Room spawnRoom) throws CheckedHibernateException {
		this.spawnRoom = spawnRoom;
		saveMap(spawnRoom.getMap());
	}

	/**
	 * Returns the {@link Room} (or null) at specified coordinates, throwing an
	 * exception if coordinates are out of bounds.
	 *
	 * @param z z coordinate of room to find
	 * @param y y coordinate of room to find
	 * @param x x coordinate of room to find
	 * @return {@link Room} at specified coordinates
	 * @throws MapExceptionOutOfBounds coordinates supplied are out of map bounds
	 */
	public Room getRoom(int z, int y, int x) throws MapExceptionOutOfBounds {
		MyLogger.log(Level.FINER, String.format("GameMap: getRoom(%s, %s, %s).", z, y, x));
		checkRoomCoordinates(z, y, x);
		return roomArray[z][y][x];
	}

	/**
	 * Attempts to get room in the direction off of the given {@link Room}.
	 * Exception occurs if the resulting coordinate would be out of bounds, or given
	 * direction is not yet supported by used functions.
	 *
	 * @param room      origin {@link Room} of which the direction points
	 * @param direction {@link Direction.DIRECTION} from origin room the sought
	 *                  after room is
	 * @return {@link Room} at specified coordinates
	 * @throws MapExceptionOutOfBounds           coordinates generated from
	 *                                           direction supplied are out of map
	 *                                           bounds
	 * @throws MapExceptionDirectionNotSupported method does not support supplied
	 *                                           direction
	 */
	public Room getRoom(Room room, Direction.DIRECTION direction)
			throws MapExceptionOutOfBounds, MapExceptionDirectionNotSupported {
		int otherZ = zAdjustDirection(room.getZ(), direction);
		int otherY = yAdjustDirection(room.getY(), direction);
		int otherX = xAdjustDirection(room.getX(), direction);
		return getRoom(otherZ, otherY, otherX);
	}

	/**
	 * Attempts to load all {@link Room}s from the database.
	 *
	 * @throws CheckedHibernateException hibernate exception
	 */
	public void loadRooms() throws CheckedHibernateException {
		synchronized (roomArray) {
			org.hibernate.Session hibSess = FireEngineMain.hibSessFactory.openSession();
			Transaction tx = null;

			try {
				tx = hibSess.beginTransaction();

				Query<?> query = hibSess.createQuery("FROM Room WHERE ROOM_MAP = :mapId");
				query.setParameter("mapId", this.getId());

				@SuppressWarnings("unchecked")
				List<Room> roomsFound = (List<Room>) query.list();
				tx.commit();

				if (roomsFound.isEmpty()) {
					MyLogger.log(Level.INFO, "GameMap: NO ROOMS FOUND"); // TODO Remove or modify temporary scaffold logging
					return;
				} else {
					MyLogger.log(Level.INFO, String.format("GameMap: %s Room(s) found.", roomsFound.size())); // TODO Remove or modify temporary
																					// scaffold logging

					for (Room foundRoom : roomsFound) {
						try {
							arrayAddRoom(foundRoom.getZ(), foundRoom.getY(), foundRoom.getX(), foundRoom);
						} catch (MapExceptionOutOfBounds e) {
							MyLogger.log(Level.WARNING,
									"GameMap: MapExceptionOutOfBounds while trying to arrayAddRoom on found room.", e);
						} catch (MapExceptionRoomExists e) {
							MyLogger.log(Level.WARNING, String.format(
									"GameMap: MapExceptionRoomExists while trying to arrayAddRoom on found room %s.",
									foundRoom.getCoordsText()), e);
						}
					}
				}

			} catch (HibernateException e) {
				if (tx != null) {
					tx.rollback();
				}
				throw new CheckedHibernateException("GameMap: Hibernate error while trying to loadRooms.", e);
			} finally {
				hibSess.close();
			}
		}
	}

	/**
	 * Inserts the given {@link Room} into the room array at the specified
	 * coordinates, throwing exception on out of bounds or if room already in that
	 * coordinate.
	 *
	 * @param z    z coordinate of room to set
	 * @param y    y coordinate of room to set
	 * @param x    x coordinate of room to set
	 * @param room room to set/add to room array
	 * @throws MapExceptionOutOfBounds coordinates provided are out of map bounds
	 * @throws MapExceptionRoomExists  room already exists at provided coordinates
	 */
	private void arrayAddRoom(int z, int y, int x, Room room) throws MapExceptionOutOfBounds, MapExceptionRoomExists {
		synchronized (roomArray) {
			Room foundRoom = getRoom(z, y, x);

			if (foundRoom != null) {
				throw new MapExceptionRoomExists(String.format(
						"MapColumn: arrayAddRoom found room already at designated coordinates (%s, %s, %s).", z, y, x));
			} else {
				roomArray[z][y][x] = room;
			}
		}
	}

	/**
	 * Attempts to create a {@link Room} at the specified coordinates.
	 *
	 * @param z z coordinate of room to create
	 * @param y y coordinate of room to create
	 * @param x x coordinate of room to create
	 * @return room created
	 * @throws MapExceptionOutOfBounds   coordinates provided are out of map bounds
	 * @throws MapExceptionRoomExists    room already exists at provided coordinates
	 * @throws CheckedHibernateException hibernate error
	 */
	public Room createRoom(int z, int y, int x)
			throws MapExceptionOutOfBounds, MapExceptionRoomExists, CheckedHibernateException {
		synchronized (roomArray) {
			checkRoomCoordinates(z, y, x);

			Room foundRoom = getRoom(z, y, x);
			if (foundRoom != null) {
				throw new MapExceptionRoomExists(String.format(
						"GameMap: createRoom found room already at designated coordinates (%s, %s, %s).", z, y, x));
			}

			Room newRoom = null;
			try {
				newRoom = Room.createRoom(this, z, x, y);
			} catch (MapExceptionRoomNull e) {
				MyLogger.log(Level.SEVERE, String.format(
						"GameMap: Weird error, MapExceptionRoomNull while trying to Room.createRoom (%s, %s, %s).", z,
						y, x), e);
				newRoom = null; // Just to make sure room to be saved isn't returned without being saved
								// properly.
				return newRoom;
			}
			try {
				arrayAddRoom(z, x, y, newRoom);
			} catch (MapExceptionRoomExists e) {
				newRoom = null;
				MyLogger.log(Level.WARNING, String.format(
						"GameMap: createRoom found room already at designated coordinates (%s, %s, %s) after creation when trying to add to room array, cleaning up.",
						z, y, x));
				try {
					Room.deleteRoom(newRoom);
				} catch (MapExceptionRoomNull e1) {
					MyLogger.log(Level.SEVERE, String.format(
							"GameMap: Weird error (already checked for), MapExceptionRoomNull while trying to deleteRoom in createRoom(%s, %s, %s).",
							z, y, x), e1);
					return newRoom;
				} catch (MapExceptionExitExists e2) {
					MyLogger.log(Level.WARNING,
							"GameMap: MapExceptionExitExists while deleteRoom after MapExceptionRoomExists on createRoom.",
							e2);
					return newRoom;
				}
				throw new MapExceptionRoomExists(
						"GameMap: createRoom found room already at designated coordinates after creation, clean up attempted.",
						e);
			}
			return newRoom;
		}
	}

	/**
	 * Attempts to create a {@link Room} in the specified direction, off of the
	 * given room.
	 *
	 * @param room      origin room to create new room off of
	 * @param direction direction to create new room off of origin room
	 * @throws MapExceptionOutOfBounds           coordinates generated from
	 *                                           direction supplied are out of map
	 *                                           bounds
	 * @throws MapExceptionRoomExists            room already exists at provided
	 *                                           coordinates
	 * @throws CheckedHibernateException         hibernate error
	 * @throws MapExceptionDirectionNotSupported method does not support supplied
	 *                                           direction
	 */
	public void createRoom(Room room, Direction.DIRECTION direction) throws MapExceptionOutOfBounds,
			MapExceptionRoomExists, CheckedHibernateException, MapExceptionDirectionNotSupported {
		int otherZ = zAdjustDirection(room.getZ(), direction);
		int otherY = yAdjustDirection(room.getY(), direction);
		int otherX = xAdjustDirection(room.getX(), direction);
		createRoom(otherZ, otherY, otherX);
	}

	/**
	 * Attempts to remove all {@link RoomExit} of {@link Room}, remove room from
	 * {@link GameMap} and delete room from database.
	 *
	 * @param z z coordinate of room to delete
	 * @param y y coordinate of room to delete
	 * @param x x coordinate of room to delete
	 * @throws MapExceptionOutOfBounds   coordinates provided are out of map bounds
	 * @throws MapExceptionRoomNull      no room found at supplied coordinates
	 * @throws CheckedHibernateException hibernate exception
	 */
	public void deleteRoom(int z, int y, int x)
			throws MapExceptionOutOfBounds, CheckedHibernateException, MapExceptionRoomNull {
		synchronized (roomArray) {

			Room foundRoom = getRoom(z, y, x);

			if (foundRoom == null) {
				throw new MapExceptionRoomNull(
						"GameMap: Tried to deleteRoom but found no room at supplied coordinates.");
			}

			for (Direction.DIRECTION direction : Direction.DIRECTION.values()) {
				try {
					removeExit(foundRoom, direction);
				} catch (MapExceptionOutOfBounds e) {
					// This is expected in some situations and is allowed
				} catch (MapExceptionExitRoomNull e) {
					// This is expected in some situations and is allowed
				} catch (MapExceptionDirectionNotSupported e) {
					MyLogger.log(Level.WARNING, "GameMap: MapExceptionDirectionNotSupported while destroyRoom.", e);
					return;
				}
			}

			try {
				Room.deleteRoom(foundRoom);
			} catch (MapExceptionExitExists e) {
				MyLogger.log(Level.WARNING,
						"GameMap: MapExceptionExitExists while deleteRoom, but all exits should have already been destroyed.",
						e);
				return;
			}

			roomArray[z][y][x] = null;
		}
	}

	/**
	 * Attempts to destroy the {@link Room} in the specified direction, off of the
	 * given room.
	 *
	 * @param room      origin room to delete off of
	 * @param direction direction off of origin room to delete
	 * @throws MapExceptionOutOfBounds           coordinates generated from
	 *                                           direction supplied are out of map
	 *                                           bounds
	 * @throws MapExceptionRoomNull              no room found at supplied
	 *                                           coordinates
	 * @throws CheckedHibernateException         hibernate exception
	 * @throws MapExceptionDirectionNotSupported method does not support supplied
	 *                                           direction
	 */
	public void deleteRoom(Room room, Direction.DIRECTION direction) throws MapExceptionOutOfBounds,
			MapExceptionRoomNull, CheckedHibernateException, MapExceptionDirectionNotSupported {
		int otherZ = zAdjustDirection(room.getZ(), direction);
		int otherY = yAdjustDirection(room.getY(), direction);
		int otherX = xAdjustDirection(room.getX(), direction);
		MyLogger.log(Level.INFO, "Destroying (" + otherZ + "," + otherY + "," + otherX + ").");
		deleteRoom(otherZ, otherY, otherX);
	}

	/**
	 * Creates an {@link RoomExit} in direction specified, from {@link Room}
	 * supplied.
	 *
	 * @param room      origin room to create exit off of
	 * @param direction direction to create exit off of origin room
	 * @throws MapExceptionRoomNull              provided origin room is null
	 * @throws MapExceptionOutOfBounds           coordinates of room at direction
	 *                                           are out of map bounds
	 * @throws MapExceptionExitRoomNull          could not find room in supplied
	 *                                           direction
	 * @throws MapExceptionExitExists            exit already exists for origin room
	 *                                           or room at direction
	 * @throws MapExceptionDirectionNotSupported direction not supported by method
	 * @throws CheckedHibernateException         hibernate exception
	 */
	public void createExit(Room room, Direction.DIRECTION direction)
			throws MapExceptionRoomNull, MapExceptionOutOfBounds, MapExceptionExitRoomNull, MapExceptionExitExists,
			MapExceptionDirectionNotSupported, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("GameMap: Tried to set exit on null room.");
		}
		if (room.getExit(direction) != null) {
			throw new MapExceptionExitExists(
					"GameMap: RoomExit is not null " + direction.toString() + " of " + room.getName() + ".");
		}

		Room otherRoom = getRoom(room, direction);
		if (otherRoom == null) {
			throw new MapExceptionExitRoomNull("GameMap: Tried to set exit on null adjacent room.");
		}
		if (room.getExit(Direction.oppositeDirection(direction)) != null) {
			throw new MapExceptionExitExists("GameMap: RoomExit is not null "
					+ Direction.oppositeDirection(direction).toString() + " of " + otherRoom.getName());
		}

		RoomExit newExit = new RoomExit();
		room.setExit(direction, newExit);
		Room.saveRoom(room);
		otherRoom.setExit(Direction.oppositeDirection(direction), newExit);
		Room.saveRoom(otherRoom);
	}

	/**
	 * Removes {@link RoomExit} in specified {@link Direction.DIRECTION}, from
	 * {@link Room} given.
	 *
	 * @param room      origin room to remove exit off of
	 * @param direction direction to remove exit off of origin room
	 * @throws MapExceptionRoomNull              provided origin room is null
	 * @throws MapExceptionOutOfBounds           coordinates of room at direction
	 *                                           are out of map bounds
	 * @throws MapExceptionExitRoomNull          could not find room in supplied
	 *                                           direction
	 * @throws MapExceptionDirectionNotSupported direction not supported by method
	 * @throws CheckedHibernateException         hibernate exception
	 */
	public void removeExit(Room room, Direction.DIRECTION direction)
			throws MapExceptionRoomNull, MapExceptionOutOfBounds, MapExceptionExitRoomNull,
			MapExceptionDirectionNotSupported, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("GameMap: Tried to remove exit on null room.");
		}
		RoomExit roomExit = room.getExit(direction);

		Room otherRoom = getRoom(room, direction);
		if (otherRoom == null) {
			throw new MapExceptionExitRoomNull("GameMap: Tried to remove exit to a null room.");
		}
		RoomExit otherRoomExit = otherRoom.getExit(Direction.oppositeDirection(direction));

		if ((roomExit != null) && (otherRoomExit != null)) {
			if (roomExit.getId() == otherRoomExit.getId()) {
				Room.deleteExit(roomExit);
			} else {
				Room.deleteExit(roomExit);
				Room.deleteExit(otherRoomExit);
			}
		} else {
			if (roomExit != null) {
				Room.deleteExit(roomExit);
			}
			if (otherRoomExit != null) {
				Room.deleteExit(otherRoomExit);
			}
		}
		room.setExit(direction, null);
		Room.saveRoom(room);
		otherRoom.setExit(Direction.oppositeDirection(direction), null);
		Room.saveRoom(otherRoom);
	}

	/**
	 * Creates a new, blank {@link GameMap} with the given name.
	 *
	 * @param name name to set on new map
	 * @return new gamemap if successful
	 * @throws CheckedHibernateException hibernate exception
	 */
	public static GameMap createMap(String name) throws CheckedHibernateException {
		GameMap newMap = new GameMap(name);

		saveMap(newMap);
		return newMap;
	}

	/**
	 * Persists the provided {@link GameMap} into the database.
	 * 
	 * TODO Have map save iterate through room array and save all rooms
	 *
	 * @param gameMap gamemap to save
	 * @throws CheckedHibernateException hibernate exception
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
	 * Generates an output object with a visual display of the {@link GameMap}
	 * surrounding the given {@link Room}.
	 *
	 * @param output Existing output object to append to, if any
	 * @param room   Room around which to display map
	 * @param size   Number of rooms in each direction to display
	 * @return ClientConnectionOutput with appended display map lines
	 */
	public static ClientConnectionOutput displayMap(ClientConnectionOutput output, Room room, int size) {
		GameMap gameMap = room.getMap();

		if (output == null) {
			output = new ClientConnectionOutput();
		}

		output.newLine();
		output.addPart("Map around \"" + room.getName() + "\" " + room.getCoordsText(), null, null);

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

			public ClientConnectionOutput buildLines(ClientConnectionOutput output, int lineZ, int lineX, int lineY,
					int lineSize) {
				String lineTop = "";
				String lineMid = "";
				String lineBot = "";

				for (int i = lineX; i < (lineX + lineSize); i++) {
					Room foundRoom;
					try {
						foundRoom = gameMap.getRoom(lineZ, lineY, i);
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
							if (foundRoom.getExit(Direction.DIRECTION.NORTHWEST) != null) {
								nw = "\\";
							}

							if (foundRoom.getExit(Direction.DIRECTION.NORTH) != null) {
								n = "|";
							}
							if (foundRoom.getExit(Direction.DIRECTION.NORTHEAST) != null) {
								ne = "/";
							}
							if (foundRoom.getExit(Direction.DIRECTION.WEST) != null) {
								w = "-";
							}
							if (foundRoom == room) {
								center = "x";
							} else if (!foundRoom.getPlayers().isEmpty()) {
								center = "o";
							}
							if (foundRoom.getExit(Direction.DIRECTION.EAST) != null) {
								e = "-";
							}
							if (foundRoom.getExit(Direction.DIRECTION.SOUTHWEST) != null) {
								sw = "/";
							}
							if (foundRoom.getExit(Direction.DIRECTION.SOUTH) != null) {
								s = "|";
							}
							if (foundRoom.getExit(Direction.DIRECTION.SOUTHEAST) != null) {
								se = "\\";
							}
						} catch (MapExceptionDirectionNotSupported e2) {
							MyLogger.log(Level.WARNING, "GameMap: MapExceptionDirectionNotSupported while displayMap.",
									e2);
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

		for (int i = (room.getY() + size); i > (room.getY() - size - 1); i--) {
			output = lineBuilder.buildLines(output, room.getZ(), (room.getX() - size), i, ((size * 2) + 1));
		}
		output.newLine();
		output.addPart(size + "x" + size + " map", null, null);

		return output;
	}

	/**
	 * Checks given coordinate is within {@link GameMap} dimensions.
	 * 
	 * TODO Update to check z, y, z against their own specific dimensions (set on
	 * map)
	 *
	 * @param z z coordinate to check
	 * @param y y coordinate to check
	 * @param x x coordinate to check
	 * @throws MapExceptionOutOfBounds one or more coordinates is out of map bounds
	 */
	public void checkRoomCoordinates(int z, int y, int x) throws MapExceptionOutOfBounds {
		if (((GameMap.MAP_DIMENSION * -1) > z) || (z > GameMap.MAP_DIMENSION)) {
			throw new MapExceptionOutOfBounds(String.format("GameMap: Failed coordinate check with z coord: %s", z));
		}
		if (((GameMap.MAP_DIMENSION * -1) > y) || (y > GameMap.MAP_DIMENSION)) {
			throw new MapExceptionOutOfBounds(String.format("GameMap: Failed coordinate check with y coord: %s", y));
		}
		if (((GameMap.MAP_DIMENSION * -1) > x) || (x > GameMap.MAP_DIMENSION)) {
			throw new MapExceptionOutOfBounds(String.format("GameMap: Failed coordinate check with x coord: %s", x));
		}
	}

	/**
	 * Returns z coordinate that occupies the given {@link Direction.DIRECTION} from
	 * the supplied coordinate.
	 *
	 * @param z         z coordinates to adjust
	 * @param direction direction to adjust for
	 * @return int of new adjusted coordinate
	 * @throws MapExceptionDirectionNotSupported direction not supported by method
	 */
	public static int zAdjustDirection(int z, Direction.DIRECTION direction) throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case UP: {
			return (z + 1);
		}
		case DOWN: {
			return (z - 1);
		}
		case NORTHWEST: {
			return (z + 0);
		}
		case NORTH: {
			return (z + 0);
		}
		case NORTHEAST: {
			return (z + 0);
		}
		case EAST: {
			return (z + 0);
		}
		case SOUTHEAST: {
			return (z + 0);
		}
		case SOUTH: {
			return (z + 0);
		}
		case SOUTHWEST: {
			return (z + 0);
		}
		case WEST: {
			return (z + 0);
		}
		default: {
			MyLogger.log(Level.WARNING,
					"GameMap: Failed to zAdjustDirection for direction: " + direction.toString() + ".");
			throw new MapExceptionDirectionNotSupported(
					"GameMap: zAdjustDirection missing case for direction " + direction.toString() + ".");
		}
		}
	}

	/**
	 * Returns y coordinate that occupies the given {@link Direction.DIRECTION} from
	 * the supplied coordinate.
	 *
	 * @param y         y coordinates to adjust
	 * @param direction direction to adjust for
	 * @return int of new adjusted coordinate
	 * @throws MapExceptionDirectionNotSupported direction not supported by method
	 */
	public static int yAdjustDirection(int y, Direction.DIRECTION direction) throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case UP: {
			return (y + 0);
		}
		case DOWN: {
			return (y + 0);
		}
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

	/**
	 * Returns x coordinate that occupies the given {@link Direction.DIRECTION} from
	 * the supplied coordinate.
	 *
	 * @param x         x coordinates to adjust
	 * @param direction direction to adjust for
	 * @return int of new adjusted coordinate
	 * @throws MapExceptionDirectionNotSupported direction not supported by method
	 */
	public static int xAdjustDirection(int x, Direction.DIRECTION direction) throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case UP: {
			return (x + 0);
		}
		case DOWN: {
			return (x + 0);
		}
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
}
