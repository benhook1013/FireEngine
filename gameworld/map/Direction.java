package fireengine.gameworld.map;

import java.util.regex.Pattern;

import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;

/*
 *    Copyright 2019 Ben Hook
 *    Direction.java
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
 * Class containing supported directions (enum) and functions related to those
 * directions.
 *
 * @author Ben Hook
 */
public class Direction {
	private static Pattern northPattern = Pattern.compile("(?i)N(?:ORTH)?");
	private static Pattern northEastPattern = Pattern.compile("(?i)N(?:ORTH)?E(?:AST)?");
	private static Pattern eastPattern = Pattern.compile("(?i)E(?:AST)?");
	private static Pattern southEastPattern = Pattern.compile("(?i)S(?:OUTH)?E(?:AST)?");
	private static Pattern southPattern = Pattern.compile("(?i)S(?:OUTH)?");
	private static Pattern southWestPattern = Pattern.compile("(?i)S(?:OUTH)?W(?:EST)?");
	private static Pattern westPattern = Pattern.compile("(?i)W(?:EST)?");
	private static Pattern northWestPattern = Pattern.compile("(?i)N(?:ORTH)?W(?:EST)?");

	/**
	 * Enum list of supported directions.
	 *
	 * @author Ben Hook
	 */
	public static enum DIRECTION {
		NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST
	}

	/**
	 * Parses directions as text into the relevant direction enum value. Returns
	 * null if no match found.
	 *
	 * @param text
	 * @return
	 */
	public static DIRECTION parseDirection(String text) {
		if (northPattern.matcher(text).matches()) {
			return DIRECTION.NORTH;
		}
		if (northEastPattern.matcher(text).matches()) {
			return DIRECTION.NORTHEAST;
		}
		if (eastPattern.matcher(text).matches()) {
			return DIRECTION.EAST;
		}
		if (southEastPattern.matcher(text).matches()) {
			return DIRECTION.SOUTHEAST;
		}
		if (southPattern.matcher(text).matches()) {
			return DIRECTION.SOUTH;
		}
		if (southWestPattern.matcher(text).matches()) {
			return DIRECTION.SOUTHWEST;
		}
		if (westPattern.matcher(text).matches()) {
			return DIRECTION.WEST;
		}
		if (northWestPattern.matcher(text).matches()) {
			return DIRECTION.NORTHWEST;
		}

		return null;
	}

	/**
	 * Returns the opposite direction to the given direction.
	 *
	 * @param direction
	 * @return
	 * @throws MapExceptionDirectionNotSupported
	 */
	public static DIRECTION oppositeDirection(DIRECTION direction) throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case NORTH: {
			return DIRECTION.SOUTH;
		}
		case NORTHEAST: {
			return DIRECTION.SOUTHWEST;
		}
		case EAST: {
			return DIRECTION.WEST;
		}
		case SOUTHEAST: {
			return DIRECTION.NORTHWEST;
		}
		case SOUTH: {
			return DIRECTION.NORTH;
		}
		case SOUTHWEST: {
			return DIRECTION.NORTHEAST;
		}
		case WEST: {
			return DIRECTION.EAST;
		}
		case NORTHWEST: {
			return DIRECTION.SOUTHEAST;
		}
		default: {
			throw new MapExceptionDirectionNotSupported(
					"Direction: oppositeDirection missing case for direction " + direction.toString());
		}
		}
	}
}
