package fireengine.gameworld.maps;

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

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.query.Query;

import fireengine.characters.Base_Character;
import fireengine.characters.player.Player_Character;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.maps.Directions.DIRECTION;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Direction_Not_Supported;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Exit_Exists;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Room_Null;
import fireengine.main.FireEngineMain;
import fireengine.utils.CheckedHibernateException;
import fireengine.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    BaseRoom.java
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
public class BaseRoom {
	/**
	 * TODO Might not need, only used to in saveRooms.
	 */
	@Transient
	private static ArrayList<BaseRoom> roomList = new ArrayList<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "B_ROOM_ID")
	private int roomId;
	@Column(name = "B_ROOM_MAP_ID")
	private int mapId;
	@Column(name = "B_ROOM_X")
	private int x;
	@Column(name = "B_ROOM_Y")
	private int y;
	@Column(name = "B_ROOM_NAME")
	private String name;
	@Column(name = "B_ROOM_DESC")
	private String desc;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_N")
	private BaseRoomExit northExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_NE")
	private BaseRoomExit northEastExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_E")
	private BaseRoomExit eastExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_SE")
	private BaseRoomExit southEastExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_S")
	private BaseRoomExit southExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_SW")
	private BaseRoomExit southWestExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_W")
	private BaseRoomExit westExit;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "B_ROOM_EXIT_NW")
	private BaseRoomExit northWestExit;

	@Transient
	private ArrayList<Player_Character> playerList;

	private BaseRoom() {
		playerList = new ArrayList<>();
	}

	public BaseRoom(int mapId, int x, int y) {
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
	public String getCoords() {
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
	 * Adds a {@link Base_Character} to the rooms player list (typically on room
	 * enter), so far only used to add {@link Player_Character}s. Always use AFTER
	 * setting room on Character, as this will check to make sure both match(check
	 * will result in hidden-to-player error).
	 * 
	 * @param player
	 */
	public void addCharacter(Base_Character player) {
		if (player.getRoom() != this) {
			MyLogger.log(Level.WARNING, "BaseRoom: addCharacter onto room that is not BaseCharacter's current room.");
		}

		if (player instanceof Player_Character) {
			synchronized (playerList) {
				if (!playerList.contains((Player_Character) player)) {
					playerList.add((Player_Character) player);
				}
			}
		}
	}

	/**
	 * Removes a {@link Base_Character} from the rooms player list (typically on
	 * room exit), so far only used to remove {@link Player_Character}s. Always use
	 * AFTER setting room on Character, as this will check to make sure isn't
	 * removing current player room(check will result in hidden-to-player error).
	 * 
	 * @param player
	 */
	public void removeCharacter(Base_Character player) {
		if (player.getRoom() == this) {
			MyLogger.log(Level.WARNING,
					"BaseRoom: removeCharacter from room that is still BaseCharacter's current room.");
		}

		if (player instanceof Player_Character) {
			synchronized (playerList) {
				playerList.remove((Player_Character) player);
			}
		}
	}

	/**
	 * Returns list of {@link Player_Character}s in the room.
	 * 
	 * @return
	 */
	public ArrayList<Player_Character> getPCs() {
		synchronized (playerList) {
			for (Player_Character pc : playerList) {
				if (pc.getRoom() != this) {
					MyLogger.log(Level.WARNING,
							"BaseRoom: Player_Character found in playerList of room, that is not current room of Player_Character. Player_Character id: "
									+ pc.getId());
				}
			}

			return new ArrayList<Player_Character>(playerList);
		}
	}

	/**
	 * Sends to listeners of room, without any exclusion.
	 * 
	 * @see BaseRoom#sendToRoom(ClientConnectionOutput, Player_Character)
	 * 
	 * @param output
	 */
	public void sendToRoom(ClientConnectionOutput output) {
		sendToRoom(output, null);
	}

	/**
	 * Sends to listeners of the room, apart from notPlayer, if one is specified.
	 * Currently only {@link Player_Character}s inside the room.
	 * 
	 * @param output
	 *            Output to be sent.
	 * @param notPlayer
	 *            Player, if specified, to be excluded from output.
	 */
	public void sendToRoom(ClientConnectionOutput output, Base_Character ignoreCharacter) {
		for (Player_Character player : playerList) {
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
	 * Returns {@link BaseRoomExit} of room for given {@link DIRECTION}.
	 * 
	 * @param direction
	 * @return
	 * @throws Map_Exception_Direction_Not_Supported
	 */
	public BaseRoomExit getExit(Directions.DIRECTION direction) throws Map_Exception_Direction_Not_Supported {
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
			throw new Map_Exception_Direction_Not_Supported(
					"BaseRoom: getExit missing case for direction " + direction.toString() + ".");
		}
		}
	}

	/**
	 * Assigns {@link BaseRoomExit} to room, of given {@link DIRECTION}.
	 * 
	 * @param direction
	 * @param newExit
	 * @throws Map_Exception_Direction_Not_Supported
	 */
	public void setExit(Directions.DIRECTION direction, BaseRoomExit newExit)
			throws Map_Exception_Direction_Not_Supported {
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
			throw new Map_Exception_Direction_Not_Supported(
					"BaseRoom: setExit missing case for direction " + direction.toString() + ".");
		}
		}
	}

	/**
	 * Returns true if room contains exit for any direction. Mainly used to test if
	 * room is safe for deletion, having had all exits removed.
	 * 
	 * @return
	 */
	protected boolean hasExit() {
		for (DIRECTION direction : DIRECTION.values()) {
			try {
				if (getExit(direction) != null) {
					return true;
				}
			} catch (Map_Exception_Direction_Not_Supported e) {
				MyLogger.log(Level.WARNING,
						"BaseRoom: Map_Exception_Direction_Not_Supported while trying to hasExit; direction: "
								+ direction.toString(),
						e);
			}
		}
		return false;
	}

	/**
	 * NOT TO BE CALLED DIRECTLY. This function does not check for existing room
	 * etc, use the room creation in the {@link GameMap} class.
	 * 
	 * Creates new {@link BaseRoom} at specified coordinates and returns new room.
	 * 
	 * @param mapId
	 * @param x
	 * @param y
	 * @return
	 * @throws Map_Exception_Room_Null
	 * @throws CheckedHibernateException
	 */
	protected static BaseRoom createRoom(int mapId, int x, int y)
			throws Map_Exception_Room_Null, CheckedHibernateException {
		BaseRoom newRoom = new BaseRoom(mapId, x, y);
		saveRoom(newRoom);
		return newRoom;
	}

	/**
	 * Saves/persists the {@link BaseRoom} into the database.
	 * 
	 * @param room
	 * @throws Map_Exception_Room_Null
	 * @throws CheckedHibernateException
	 */
	protected static void saveRoom(BaseRoom room) throws Map_Exception_Room_Null, CheckedHibernateException {
		if (room == null) {
			throw new Map_Exception_Room_Null("BaseRoom: Tried to deleteRoom on a null room.");
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
			throw new CheckedHibernateException("BaseRoom: Hibernate error while trying to saveRoom.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}

	/**
	 * TODO Consider usage of this. Save on Maps should do this behaviour.
	 * 
	 * Saves all rooms on list of all rooms.
	 * 
	 * @throws Map_Exception_Room_Null
	 * @throws CheckedHibernateException
	 */
	protected static void saveRooms() throws Map_Exception_Room_Null, CheckedHibernateException {
		for (BaseRoom room : roomList) {
			saveRoom(room);
		}
	}

	/**
	 * DO NOT CALL DIRECTLY. Removes {@link BaseRoom} from database but does not
	 * remove room from {@link MapColumn} etc, meaning it could be re-saved. Use the
	 * room destroy function in {@link GameMap} instead.
	 * 
	 * @param room
	 * @throws Map_Exception_Room_Null
	 * @throws Map_Exception_Exit_Exists
	 * @throws CheckedHibernateException
	 */
	protected static void deleteRoom(BaseRoom room)
			throws Map_Exception_Room_Null, Map_Exception_Exit_Exists, CheckedHibernateException {
		if (room == null) {
			throw new Map_Exception_Room_Null("BaseRoom: Tried to deleteRoom on a null room.");
		}
		if (room.hasExit()) {
			throw new Map_Exception_Exit_Exists("BaseRoom: Tried to deleteRoom that still had exits.");
		}

		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("DELETE FROM BaseRoom WHERE B_ROOM_ID = :id");
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

	protected static void deleteExit(BaseRoomExit exit) throws Map_Exception_Room_Null, CheckedHibernateException {
		if (exit == null) {
			throw new Map_Exception_Room_Null("BaseRoom: Tried to deleteExit on a null exit.");
		}

		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("DELETE FROM BaseRoomExit WHERE B_ROOM_EXIT_ID = :id");
			query.setParameter("id", exit.getId());
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
