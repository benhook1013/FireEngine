package fireengine.gameworld.map.room;

import java.util.ArrayList;
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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.query.Query;

import fireengine.character.BaseCharacter;
import fireengine.character.player.PlayerCharacter;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.GameMap;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.Direction.DIRECTION;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exception.MapExceptionExitExists;
import fireengine.gameworld.map.exception.MapExceptionRoomNull;
import fireengine.gameworld.map.exit.RoomExit;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    Room.java
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
 * Default room type, with standard room functions.
 *
 * @author Ben Hook
 */
@Entity
@Table(name = "BASE_ROOM")
public class Room {
//	/**
//	 * TODO Might not need, only used to in saveRooms. NOT CURRENTLY USED. TODO Remove
//	 */
//	@Transient
//	private static ArrayList<Room> roomList = new ArrayList<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "B_ROOM_ID", nullable = false)
	@NotNull
	private int roomId;

	@Column(name = "B_ROOM_MAP_ID", nullable = false)
	@NotNull
	private int mapId;

	@Column(name = "B_ROOM_X", nullable = false)
	@NotNull
	private int x;

	@Column(name = "B_ROOM_Y", nullable = false)
	@NotNull
	private int y;

	@Column(name = "B_ROOM_NAME")
	@NotNull
	private String name;

	@Column(name = "B_ROOM_DESC")
	@NotNull
	private String desc;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_N")
	private RoomExit northExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_NE")
	private RoomExit northEastExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_E")
	private RoomExit eastExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_SE")
	private RoomExit southEastExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_S")
	private RoomExit southExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_SW")
	private RoomExit southWestExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_W")
	private RoomExit westExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_NW")
	private RoomExit northWestExit;

	@Transient
	private ArrayList<PlayerCharacter> playerList;

	private Room() {
		playerList = new ArrayList<>();
	}

	public Room(int mapId, int x, int y) {
		this();
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.name = "Placeholder name";
		this.desc = "Placeholder desc.";
	}

	public int getRoomId() {
		return roomId;
	}

	@SuppressWarnings("unused")
	private void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getMapId() {
		return mapId;
	}

	@SuppressWarnings("unused")
	private void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getX() {
		return x;
	}

	@SuppressWarnings("unused")
	private void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	@SuppressWarnings("unused")
	private void setY(int y) {
		this.y = y;
	}

	/**
	 * Returns a String that looks like "(x,y)".
	 *
	 * @return
	 */
	public String getCoordsText() {
		return "(" + getX() + "," + getY() + ")";
	}

	public String getRoomName() {
		return name;
	}

	public void setRoomName(String name) {
		this.name = name;
	}

	public String getRoomDesc() {
		return desc;
	}

	public void setRoomDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * Adds a {@link BaseCharacter} to the {@link Room}'s player list (typically
	 * on room enter), so far only used to add {@link PlayerCharacter}s. Always use
	 * AFTER setting room on Character, as this will check to make sure both
	 * match(check will result in hidden-to-player error).
	 *
	 * @param player
	 */
	public void addCharacter(BaseCharacter player) {
		if (player.getRoom() != this) {
			MyLogger.log(Level.WARNING, "Room: addCharacter onto room that is not BaseCharacter's current room.");
		}

		if (player instanceof PlayerCharacter) {
			synchronized (playerList) {
				if (!playerList.contains(player)) {
					playerList.add((PlayerCharacter) player);
				}
			}
		} else {
			MyLogger.log(Level.WARNING, "Room: addCharacter on BaseCharacter type that is not a player.");
		}
	}

	/**
	 * Removes a {@link BaseCharacter} from the rooms player list (typically on room
	 * exit), so far only used to remove {@link PlayerCharacter}s. Always use AFTER
	 * setting room on Character, as this will check to make sure isn't removing
	 * current player room(check will result in hidden-to-player error).
	 *
	 * @param player
	 */
	public void removeCharacter(BaseCharacter player) {
		if (player.getRoom() == this) {
			MyLogger.log(Level.WARNING,
					"Room: removeCharacter from room that is still BaseCharacter's current room.");
		}

		if (player instanceof PlayerCharacter) {
			synchronized (playerList) {
				playerList.remove(player);
			}
		}
	}

	/**
	 * Returns list of {@link PlayerCharacter}s in the room.
	 *
	 * @return
	 */
	public ArrayList<PlayerCharacter> getPCs() {
		synchronized (playerList) {
			for (PlayerCharacter pc : playerList) {
				if (pc.getRoom() != this) {
					MyLogger.log(Level.WARNING,
							"Room: PlayerCharacter found in playerList of room, that is not current room of PlayerCharacter. PlayerCharacter id: "
									+ pc.getId());
				}
			}

			return new ArrayList<>(playerList);
		}
	}

	/**
	 * Sends to listeners of room, without any exclusion.
	 *
	 * @see Room#sendToRoomExcluding(ClientConnectionOutput, PlayerCharacter)
	 *
	 * @param output
	 */
	public void sendToRoom(ClientConnectionOutput output) {
		sendToRoomExcluding(output, null);
	}

	/**
	 * Sends to listeners of the room, apart from notPlayer, if one is specified.
	 * Currently only {@link PlayerCharacter}s inside the room.
	 *
	 * @param output    Output to be sent.
	 * @param notPlayer Player, if specified, to be excluded from output.
	 */
	public void sendToRoomExcluding(ClientConnectionOutput output, BaseCharacter ignoreCharacter) {
		for (PlayerCharacter player : playerList) {
			if (ignoreCharacter == null) {
				player.sendToListeners(new ClientConnectionOutput(output));
			} else {
				if (!(player == ignoreCharacter)) {
					player.sendToListeners(new ClientConnectionOutput(output));
				}
			}
		}
	}

	/**
	 * Returns {@link RoomExit} of {@link Room} for given
	 * {@link Direction.DIRECTION}.
	 *
	 * @param direction
	 * @return
	 * @throws MapExceptionDirectionNotSupported
	 */
	public RoomExit getExit(Direction.DIRECTION direction) throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case NORTH: {
			return northExit;
		}
		case NORTHEAST: {
			return northEastExit;
		}
		case EAST: {
			return eastExit;
		}
		case SOUTHEAST: {
			return southEastExit;
		}
		case SOUTH: {
			return southExit;
		}
		case SOUTHWEST: {
			return southWestExit;
		}
		case WEST: {
			return westExit;
		}
		case NORTHWEST: {
			return northWestExit;
		}
		default: {
			throw new MapExceptionDirectionNotSupported(
					"Room: getExit missing case for direction " + direction.toString() + ".");
		}
		}
	}

	/**
	 * Assigns {@link RoomExit} to {@link Room}, of given
	 * {@link Direction.DIRECTION}.
	 *
	 * @param direction
	 * @param newExit
	 * @throws MapExceptionDirectionNotSupported
	 */
	public void setExit(Direction.DIRECTION direction, RoomExit newExit) throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case NORTH: {
			this.northExit = newExit;
		}
		case NORTHEAST: {
			this.northEastExit = newExit;
		}
		case EAST: {
			this.eastExit = newExit;
		}
		case SOUTHEAST: {
			this.southEastExit = newExit;
		}
		case SOUTH: {
			this.southExit = newExit;
		}
		case SOUTHWEST: {
			this.southWestExit = newExit;
		}
		case WEST: {
			this.westExit = newExit;
		}
		case NORTHWEST: {
			this.northWestExit = newExit;
		}
		default: {
			throw new MapExceptionDirectionNotSupported(
					"Room: setExit missing case for direction " + direction.toString() + ".");
		}
		}
	}

	/**
	 * Returns true if {@link Room} contains {@link RoomExit} for any
	 * {@link Direction.DIRECTION}. Mainly used to test if room is safe for
	 * deletion, having had all exits removed.
	 *
	 * @return
	 */
	protected boolean hasExit() {
		for (DIRECTION direction : DIRECTION.values()) {
			try {
				if (getExit(direction) != null) {
					return true;
				}
			} catch (MapExceptionDirectionNotSupported e) {
				MyLogger.log(Level.WARNING,
						"Room: MapExceptionDirectionNotSupported while trying to hasExit; direction: "
								+ direction.toString(),
						e);
			}
		}
		return false;
	}

	/**
	 * NOT TO BE CALLED DIRECTLY. This function does not check for existing room
	 * etc; use the room creation in the {@link GameMap} class.
	 *
	 * Creates new {@link Room} at specified coordinates and returns new room.
	 *
	 * @param mapId
	 * @param x
	 * @param y
	 * @return
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 */
	public static Room createRoom(int mapId, int x, int y) throws MapExceptionRoomNull, CheckedHibernateException {
		Room newRoom = new Room(mapId, x, y);
		saveRoom(newRoom);
		return newRoom;
	}

	/**
	 * Saves/persists the {@link Room} into the database.
	 *
	 * @param room
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 */
	public static void saveRoom(Room room) throws MapExceptionRoomNull, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("Room: Tried to deleteRoom on a null room.");
		}

		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			hibSess.saveOrUpdate(room);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Room: Hibernate error while trying to saveRoom.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}

//	/**
//	 * TODO Consider usage of this. Save on Maps should do this behaviour. NOT
//	 * CURRENTLY USED. TODO Remove
//	 *
//	 * Saves all rooms on list of all rooms.
//	 *
//	 * @throws MapExceptionRoomNull
//	 * @throws CheckedHibernateException
//	 */
//	protected static void saveRooms() throws MapExceptionRoomNull, CheckedHibernateException {
//		for (Room room : roomList) {
//			saveRoom(room);
//		}
//	}

	/**
	 * DO NOT CALL DIRECTLY. Removes {@link Room} from database but does not
	 * remove room from {@link MapColumn} etc, meaning it could be re-saved. Use the
	 * room destroy function in {@link GameMap} instead.
	 *
	 * @param room
	 * @throws MapExceptionRoomNull
	 * @throws MapExceptionExitExists
	 * @throws CheckedHibernateException
	 */
	public static void deleteRoom(Room room)
			throws MapExceptionRoomNull, MapExceptionExitExists, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("Room: Tried to deleteRoom on a null room.");
		}
		if (room.hasExit()) {
			throw new MapExceptionExitExists("Room: Tried to deleteRoom that still had exits.");
		}

		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("DELETE FROM Room WHERE B_ROOM_ID = :id");
			query.setParameter("id", room.getRoomId());
			query.executeUpdate();

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("MapColumn: Hibernate error when trying to deleteRoom.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}

	/**
	 * DO NOT CALL DIRECTLY. Removes {@link RoomExit} from database. Call
	 * {@link GameMap#destroyExit(Room, DIRECTION)} instead.
	 * 
	 * @param roomExit
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 */
	public static void deleteExit(RoomExit roomExit) throws MapExceptionRoomNull, CheckedHibernateException {
		if (roomExit == null) {
			throw new MapExceptionRoomNull("Room: Tried to deleteExit on a null exit.");
		}

		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("DELETE FROM RoomExit WHERE B_ROOM_EXIT_ID = :id");
			query.setParameter("id", roomExit.getId());
			query.executeUpdate();

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("MapColumn: Hibernate error when trying to deleteRoom.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}
}
