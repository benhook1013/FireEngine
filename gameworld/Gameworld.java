package fireengine.gameworld;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import fireengine.gameworld.map.GameMap;
import fireengine.gameworld.map.exception.MapExceptionMapLoad;
import fireengine.gameworld.map.exception.MapExceptionOutOfBounds;
import fireengine.gameworld.map.exception.MapExceptionRoomExists;
import fireengine.gameworld.map.exception.MapExceptionRoomLoad;
import fireengine.gameworld.map.room.BaseRoom;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    Gameworld.java
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
 * The container and manager for {@link GameMap}s in the game.
 *
 * @author Ben Hook
 */
public class Gameworld {
	// TODO This should probably be a hashmap etc based on map id
	private static ArrayList<GameMap> mapList = new ArrayList<>();

	public static void setupGameworld() throws CheckedHibernateException, MapExceptionMapLoad {
		loadMaps();

		GameMap mainMap = findMap("Mainland");

		if (mainMap == null) {
			System.out.println("Creating new map: Mainland");
			try {
				GameMap.createMap("Mainland");
			} catch (CheckedHibernateException e) {
				FireEngineMain.hibernateException(e);
				return;
			}
		} else {
			System.out.println("Using old map: " + mainMap.getName());
		}
	}

	/**
	 * Attempts to load {@link GameMap}s from the database.
	 *
	 * @throws CheckedHibernateException
	 * @throws MapExceptionMapLoad
	 */
	private static void loadMaps() throws CheckedHibernateException, MapExceptionMapLoad {
		org.hibernate.Session hibSess = FireEngineMain.hibSessFactory.openSession();
		Transaction tx = null;

		try {
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("FROM GameMap");

			@SuppressWarnings("unchecked")
			List<GameMap> mapsFound = (List<GameMap>) query.list();
			tx.commit();

			if (mapsFound.isEmpty()) {
				System.out.println("NO MAPS FOUND");
				return;
			} else {
				System.out.println(mapsFound.size() + " MAPS FOUND");

				synchronized (mapList) {
					for (GameMap foundMap : mapsFound) {
						addMap(foundMap);
					}
				}
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Gameworld: Hibernate error while trying to loadMaps.", e);
		} finally {
			hibSess.close();
		}

		synchronized (mapList) {
			for (GameMap foundMap : mapList) {
				try {
					loadRooms(foundMap.getId());
				} catch (MapExceptionRoomLoad e) {
					throw new MapExceptionMapLoad(
							"Gameworld: Tried to loadMaps but encountered MapExceptionRoomLoad from loadRooms.", e);
				}
			}
		}
	}

	/**
	 * Attempts to load all {@link BaseRoom}s from the database, give supplied map
	 * id.
	 *
	 * @param mapId int id of map to load
	 * @throws CheckedHibernateException
	 * @throws MapExceptionRoomLoad
	 */
	private static void loadRooms(int mapId) throws CheckedHibernateException, MapExceptionRoomLoad {
		GameMap gameMap = findMap(mapId);

		if (gameMap == null) {
			throw new MapExceptionRoomLoad(
					"Gameworld: Tried to loadRooms but cannot findMap with mapId " + mapId + ".");
		}

		org.hibernate.Session hibSess = FireEngineMain.hibSessFactory.openSession();
		Transaction tx = null;

		try {
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("FROM BaseRoom WHERE mapId = :mapId");
			query.setParameter("mapId", mapId);

			@SuppressWarnings("unchecked")
			List<BaseRoom> roomsFound = (List<BaseRoom>) query.list();
			tx.commit();

			if (roomsFound.isEmpty()) {
				System.out.println("NO ROOMS FOUND");
				return;
			} else {
				System.out.println(roomsFound.size() + " ROOMS FOUND");

				for (BaseRoom foundRoom : roomsFound) {
					try {
						gameMap.setRoom(foundRoom.getX(), foundRoom.getY(), foundRoom);
					} catch (MapExceptionOutOfBounds e) {
						MyLogger.log(Level.WARNING,
								"Gameworld: MapExceptionOutOfBounds while trying to setRoom on found room.", e);
					} catch (MapExceptionRoomExists e) {
						MyLogger.log(Level.WARNING,
								"Gameworld: MapExceptionRoomExists while trying to setRoom on found room "
										+ foundRoom.getCoordsText() + ".",
								e);
					}
				}
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Gameworld: Hibernate error while trying to loadRooms.", e);
		} finally {
			hibSess.close();
		}
	}

	/**
	 * Searches in memory for {@link GameMap}s with matching name. It should not be
	 * the case that there are maps in the database not loaded.
	 *
	 * @param name String name of map to find
	 * @return
	 */
	public static GameMap findMap(String name) {
		synchronized (mapList) {
			for (GameMap foundMap : mapList) {
				if (foundMap.getName().equals(name)) {
					return foundMap;
				}
			}
			return null;
		}
	}

	/**
	 * Searches in memory for {@link GameMap}s with matching id. It should not be
	 * the case that there are maps in the database not loaded.
	 *
	 * @param id int id of map to find
	 * @return
	 */
	public static GameMap findMap(int id) {
		synchronized (mapList) {
			for (GameMap foundMap : mapList) {
				if (foundMap.getId() == id) {
					return foundMap;
				}
			}
			return null;
		}
	}

	/**
	 * Adds a {@link GameMap} to the map list, not allowing duplicates based on id.
	 *
	 * @param gameMap {@link GameMap} to add to map list
	 */
	public static void addMap(GameMap gameMap) {
		synchronized (mapList) {
			for (GameMap foundMap : mapList) {
				if (foundMap.getId() == gameMap.getId()) {
					// TODO probably throw error.
					return;
				}
			}
			mapList.add(gameMap);
		}
	}
}
