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
 * CharacterClass containing supported directions (enum) and functions related
 * to those directions.
 *
 * @author Ben Hook
 */
public class Direction {
	private static Pattern upPattern = Pattern.compile("U(?:P)?", Pattern.CASE_INSENSITIVE);
	private static Pattern downPattern = Pattern.compile("D(?:OWN)?", Pattern.CASE_INSENSITIVE);
	private static Pattern northPattern = Pattern.compile("N(?:ORTH)?", Pattern.CASE_INSENSITIVE);
	private static Pattern northEastPattern = Pattern.compile("N(?:ORTH)?E(?:AST)?", Pattern.CASE_INSENSITIVE);
	private static Pattern eastPattern = Pattern.compile("E(?:AST)?", Pattern.CASE_INSENSITIVE);
	private static Pattern southEastPattern = Pattern.compile("S(?:OUTH)?E(?:AST)?", Pattern.CASE_INSENSITIVE);
	private static Pattern southPattern = Pattern.compile("S(?:OUTH)?", Pattern.CASE_INSENSITIVE);
	private static Pattern southWestPattern = Pattern.compile("S(?:OUTH)?W(?:EST)?", Pattern.CASE_INSENSITIVE);
	private static Pattern westPattern = Pattern.compile("W(?:EST)?", Pattern.CASE_INSENSITIVE);
	private static Pattern northWestPattern = Pattern.compile("N(?:ORTH)?W(?:EST)?", Pattern.CASE_INSENSITIVE);

	/**
	 * Enum list of supported directions.
	 *
	 * @author Ben Hook
	 */
	public static enum DIRECTION {
		UP, DOWN, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST
	}

	/**
	 * Parses directions as text into the relevant direction enum value. Returns
	 * null if no match found.
	 *
	 * @param text
	 * @return
	 */
	public static DIRECTION parseDirection(String text) {
		if (upPattern.matcher(text).matches()) {
			return DIRECTION.UP;
		}
		if (downPattern.matcher(text).matches()) {
			return DIRECTION.DOWN;
		}
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
		case UP: {
			return DIRECTION.DOWN;
		}
		case DOWN: {
			return DIRECTION.UP;
		}
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
