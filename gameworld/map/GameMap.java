package fireengine.gameworld.map;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.exception.MapExceptionCoordinateNull;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exception.MapExceptionExitExists;
import fireengine.gameworld.map.exception.MapExceptionExitRoomNull;
import fireengine.gameworld.map.exception.MapExceptionRoomExists;
import fireengine.gameworld.map.exception.MapExceptionRoomNull;
import fireengine.gameworld.map.exit.RoomExit;
import fireengine.gameworld.map.room.Room;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.IDSequenceGenerator;
import fireengine.util.MyLogger;

import java.util.stream.Collectors;

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
	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	/**
	 * It is important this does not change during runtime as it is used in the
	 * hashCode method.
	 */
	@Column(name = "NAME", nullable = false)
	@NotNull
	private String name;

	// Example Hibernate mapping for 'Bidirectional @OneToMany'
	// https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/
	// https://www.baeldung.com/hibernate-persisting-maps
	// TODO Consider implementation to prevent multiple Rooms being in set Values.
	// To be noted, when Hibernate
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "COORD_ROOM_MAPPING", 
	joinColumns = {@JoinColumn(name = "GAMEMAP", referencedColumnName = "id") }, 
	inverseJoinColumns = {@JoinColumn(name = "ROOM", referencedColumnName = "id") })
	@MapKeyJoinColumn(name = "COORDINATE")
	private Map<Coordinate, Room> rooms;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SPAWN_ROOM")
	private Room spawnRoom;

	@SuppressWarnings("unused")
	private GameMap() {
	}

	public GameMap(String name) {
		id = IDSequenceGenerator.getNextID("GameMap");
		rooms = new ConcurrentHashMap<Coordinate, Room>();
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Room getSpawnRoom() {
		return this.spawnRoom;
	}

	public Room getSpawnRoomOrCentre() {
		Room returnRoom = this.spawnRoom;
		if (returnRoom == null) {
			returnRoom = getRoom(0, 0, 0);
		}

		return returnRoom;
	}

	public void setSpawnRoom(Room spawnRoom) throws CheckedHibernateException {
		this.spawnRoom = spawnRoom;
		saveMap(spawnRoom.getMap());
	}

	/**
	 * Returns the {@link Room} (or null) at specified Coordinate. Use
	 * {@link GameMap#getRoom(int, int, int)} to get based on coordinates.
	 *
	 * @param z z coordinate of room to find
	 * @param y y coordinate of room to find
	 * @param x x coordinate of room to find
	 * @return {@link Room} at specified coordinates
	 */
	@SuppressWarnings("unused")
	private Room getRoom(Coordinate coord) {
		MyLogger.log(Level.FINER,
				String.format("GameMap: getRoom(%s, %s, %s).", coord.getX(), coord.getY(), coord.getZ()));
		return rooms.get(coord);
	}

	/**
	 * Used to get a Room belonging to the GameMap with given coordinates (separate
	 * for searching on the Coordinate object; see
	 * {@link GameMap#getRoom(Coordinate)}).
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Room getRoom(int x, int y, int z) {
		for (Entry<Coordinate, Room> mapEntry : rooms.entrySet()) {
			if ((mapEntry.getKey().getX() == x) && (mapEntry.getKey().getY() == y) && (mapEntry.getKey().getZ() == z)) {
				return mapEntry.getValue();
			}
		}
		return null;
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
	 * @throws MapExceptionDirectionNotSupported method does not support supplied
	 *                                           direction
	 */
	public Room getRoom(Room room, Direction.DIRECTION direction) throws MapExceptionDirectionNotSupported {
		Coordinate coord = room.getCoord();

		int otherX = xAdjustDirection(coord.getX(), direction);
		int otherY = yAdjustDirection(coord.getY(), direction);
		int otherZ = zAdjustDirection(coord.getZ(), direction);

		return getRoom(otherX, otherY, otherZ);
	}

	public int getRoomCount() {
		return rooms.size();
	}

	public Coordinate getCoord(Room room) {
		List<Coordinate> foundList = rooms.entrySet().stream().filter(entry -> room.equals(entry.getValue()))
				.map(Map.Entry::getKey).collect(Collectors.toList());
		if (foundList.size() > 1) {
			MyLogger.log(Level.WARNING, "GameMap: getCoord(Room) returned more than one matching Coordiante.");
		}
		if (foundList.size() < 1) {
			return null;
		}
		return foundList.get(0);
	}

	/**
	 * Used to get a Coordinate belonging to the GameMap with given coordinates.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Coordinate getCoord(int x, int y, int z) {
		for (Entry<Coordinate, Room> mapEntry : rooms.entrySet()) {
			if ((mapEntry.getKey().getX() == x) && (mapEntry.getKey().getY() == y) && (mapEntry.getKey().getZ() == z)) {
				return mapEntry.getKey();
			}
		}
		return null;
	}

	/**
	 * Attempts to create a {@link Room} at the specified coordinates.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return room created
	 * @throws MapExceptionRoomExists    room already exists at provided coordinates
	 * @throws CheckedHibernateException hibernate error
	 */
	public Room createRoom(int x, int y, int z) throws MapExceptionRoomExists, CheckedHibernateException {
		Room foundRoom = getRoom(x, y, z);
		if (foundRoom != null) {
			throw new MapExceptionRoomExists(String
					.format("GameMap: createRoom found room already at designated coordinates (%d, %d, %d).", x, y, z));
		}

		Room newRoom = null;
		newRoom = Room.createRoom(this);

		Coordinate newCoord = Coordinate.createCoord(this, x, y, z);
		rooms.put(newCoord, newRoom);
		saveMap(this);

		return newRoom;
	}

	/**
	 * Attempts to create a {@link Room} in the specified direction, off of the
	 * given room.
	 *
	 * @param room      origin room to create new room off of
	 * @param direction direction to create new room off of origin room
	 * @throws MapExceptionRoomExists            room already exists at provided
	 *                                           coordinates
	 * @throws CheckedHibernateException         hibernate error
	 * @throws MapExceptionDirectionNotSupported method does not support supplied
	 *                                           direction
	 */
	public void createRoom(Room room, Direction.DIRECTION direction)
			throws MapExceptionRoomExists, CheckedHibernateException, MapExceptionDirectionNotSupported {
		Coordinate coord = room.getCoord();

		int otherX = xAdjustDirection(coord.getX(), direction);
		int otherY = yAdjustDirection(coord.getY(), direction);
		int otherZ = zAdjustDirection(coord.getZ(), direction);

		createRoom(otherX, otherY, otherZ);
	}

	/**
	 * Attempts to remove all {@link RoomExit} of {@link Room}, remove room from
	 * {@link GameMap} and delete room from database.
	 *
	 * @param z z coordinate of room to delete
	 * @param y y coordinate of room to delete
	 * @param x x coordinate of room to delete
	 * @throws MapExceptionRoomNull      no room found at supplied coordinates
	 * @throws CheckedHibernateException hibernate exception
	 */
	public void deleteRoom(int x, int y, int z) throws CheckedHibernateException, MapExceptionRoomNull {
		Room foundRoom = getRoom(x, y, z);

		if (foundRoom == null) {
			throw new MapExceptionRoomNull("GameMap: Tried to deleteRoom but found no room at supplied Coordinate.");
		}

		// TODO Don't think the below should be necessary if mapped properly. Possibly
		// need cascade + orphan removal.
		for (Direction.DIRECTION direction : Direction.DIRECTION.values()) {
			try {
				deleteExit(foundRoom, direction);
			} catch (MapExceptionExitRoomNull e) {
				// This is expected in some situations and is allowed
			} catch (MapExceptionDirectionNotSupported e) {
				MyLogger.log(Level.WARNING, "GameMap: MapExceptionDirectionNotSupported while destroyRoom.", e);
				return;
			}
		}

		Coordinate coord = getCoord(foundRoom);
		try {
			rooms.remove(coord);
		} catch (NullPointerException e) {
			MyLogger.log(Level.WARNING,
					"GameMap: Null Coordinate when trying to remove Coordinate/Room entry from rooms Map.", e);
		}
		try {
			Coordinate.deleteCoord(coord);
		} catch (MapExceptionCoordinateNull e) {
			MyLogger.log(Level.WARNING, "GameMap: Coordinate null when trying to Hibernate delete the Coordinate.", e);
		}
		saveMap(this);
	}

	/**
	 * Attempts to destroy the {@link Room} in the specified direction, off of the
	 * given room.
	 *
	 * @param room      origin room to delete off of
	 * @param direction direction off of origin room to delete
	 * @throws MapExceptionRoomNull              no room found at supplied
	 *                                           coordinates
	 * @throws CheckedHibernateException         hibernate exception
	 * @throws MapExceptionDirectionNotSupported method does not support supplied
	 *                                           direction
	 */
	public void deleteRoom(Room room, Direction.DIRECTION direction)
			throws MapExceptionRoomNull, CheckedHibernateException, MapExceptionDirectionNotSupported {
		Coordinate coord = room.getCoord();

		int otherX = xAdjustDirection(coord.getX(), direction);
		int otherY = yAdjustDirection(coord.getY(), direction);
		int otherZ = zAdjustDirection(coord.getZ(), direction);

		deleteRoom(otherX, otherY, otherZ);
	}

	/**
	 * Creates an {@link RoomExit} in direction specified, from {@link Room}
	 * supplied.
	 *
	 * @param room      origin room to create exit off of
	 * @param direction direction to create exit off of origin room
	 * @throws MapExceptionRoomNull              provided origin room is null
	 * @throws MapExceptionExitRoomNull          could not find room in supplied
	 *                                           direction
	 * @throws MapExceptionExitExists            exit already exists for origin room
	 *                                           or room at direction
	 * @throws MapExceptionDirectionNotSupported direction not supported by method
	 * @throws CheckedHibernateException         hibernate exception
	 */
	public void createExit(Room room, Direction.DIRECTION direction)
			throws MapExceptionRoomNull, MapExceptionExitRoomNull, MapExceptionExitExists,
			MapExceptionDirectionNotSupported, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("GameMap: Tried to set exit on null room.");
		}
		if (room.getExit(direction) != null) {
			throw new MapExceptionExitExists(
					String.format("GameMap: RoomExit is not null %s of %s.", direction.toString(), room.getName()));
		}

		Room otherRoom = getRoom(room, direction);
		if (otherRoom == null) {
			throw new MapExceptionExitRoomNull("GameMap: Tried to set exit on null adjacent room.");
		}
		if (otherRoom.getExit(Direction.oppositeDirection(direction)) != null) {
			throw new MapExceptionExitExists(String.format("GameMap: RoomExit is not null %s of %s.",
					Direction.oppositeDirection(direction).toString(), otherRoom.getName()));
		}

		RoomExit newExit = new RoomExit(true);
		// TODO If fails to set exit, delete partway created exit.
		room.setExit(direction, newExit);
		otherRoom.setExit(Direction.oppositeDirection(direction), newExit);
	}

	/**
	 * Removes {@link RoomExit} in specified {@link Direction.DIRECTION}, from
	 * {@link Room} given.
	 *
	 * @param room      origin room to remove exit off of
	 * @param direction direction to remove exit off of origin room
	 * @throws MapExceptionRoomNull              provided origin room is null
	 * @throws MapExceptionExitRoomNull          could not find room in supplied
	 *                                           direction
	 * @throws MapExceptionDirectionNotSupported direction not supported by method
	 * @throws CheckedHibernateException         hibernate exception
	 */
	public void deleteExit(Room room, Direction.DIRECTION direction) throws MapExceptionRoomNull,
			MapExceptionExitRoomNull, MapExceptionDirectionNotSupported, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("GameMap: Tried to remove exit on null room.");
		}

		Room otherRoom = getRoom(room, direction);
		if (otherRoom == null) {
			throw new MapExceptionExitRoomNull("GameMap: Tried to remove exit to a null room.");
		}

		room.setExit(direction, null);
		otherRoom.setExit(Direction.oppositeDirection(direction), null);

		saveMap(this);
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
	 * @param radius Number of rooms in each direction to display
	 * @return ClientConnectionOutput with appended display map lines
	 */
	public static ClientConnectionOutput displayMap(Room room, int radius) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		class Line_Builder {
			public ClientConnectionOutput buildLines(ClientConnectionOutput output, GameMap gameMap, int lineX,
					int lineY, int lineZ, int lineSize) {
				String lineTop = "";
				String lineMid = "";
				String lineBot = "";

				for (int i = lineX - lineSize; i <= (lineX + lineSize); i++) {
					Room foundRoom;
					foundRoom = gameMap.getRoom(i, lineY, lineZ);

					if (foundRoom == null) {
						lineTop = lineTop + "     ";
						lineMid = lineMid + "  *  ";
						lineBot = lineBot + "     ";
					} else {
						String u = "]";
						String d = "[";
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
							if (foundRoom.getExit(Direction.DIRECTION.UP) != null) {
								u = "^";
							}
							if (foundRoom.getExit(Direction.DIRECTION.DOWN) != null) {
								d = "v";
							}
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
						lineMid = lineMid + w + d + center + u + e;
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

		GameMap gameMap = room.getMap();

		output.newLine();
		output.addPart(String.format("Map around \"%s\" %s with radius %s", room.getName(), room.getCoord().toString(),
				radius), null, null);

		Line_Builder lineBuilder = new Line_Builder();

		for (int i = (room.getCoord().getY() + radius); i > (room.getCoord().getY() - radius - 1); i--) {
			output = lineBuilder.buildLines(output, gameMap, room.getCoord().getX(), i, room.getCoord().getZ(), radius);
		}
		output.newLine();

		return output;
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

	// TODO Should probably move the coordinate manipulation stuff into Coordinate
	// class.
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
	 * Custom implementation requires for proper JPA/Hibernate function.
	 * <p>
	 * See relevant information or both hashCode and equals in
	 * {@link Room#hashCode()}
	 * </p>
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = (prime * result) + getId();
		return result;
	}

	/**
	 * Custom implementation requires for proper JPA/Hibernate function.
	 * 
	 * <p>
	 * See relevant information or both hashCode and equals in
	 * {@link Room#hashCode()}
	 * </p>
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameMap other = (GameMap) obj;
		if (this.getId() == other.getId()) {
			return true;
		}
		return false;
	}
}
