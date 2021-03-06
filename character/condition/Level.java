package fireengine.character.condition;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fireengine.character.condition.exception.LevelExceptionAlreadyMax;
import fireengine.character.condition.exception.LevelExceptionAlreadyMin;
import fireengine.gameworld.map.Coordinate;
import fireengine.gameworld.map.room.Room;
import fireengine.util.IDSequenceGenerator;

/*
 *    Copyright 2019 Ben Hook
 *    Level.java
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

@Entity
@Table(name = "CHAR_LEVEL")
public class Level {
	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	private static int[] experienceList = { 0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1150, 1350, 1600,
			1900, 2250, 2650, 3100, 3600, 4150 };
	private static int baseStat = 100;
	private static int statPerLevel = 30;

	@Column(name = "LEVEL", nullable = false)
	@NotNull
	private int level;

	@Column(name = "EXPERIENCE", nullable = false)
	@NotNull
	private int experience;

	@SuppressWarnings("unused")
	private Level() {
	}

	public Level(int level) {
		this(level, 0);
	}

	public Level(int level, int experience) {
		synchronized (this) {
			id = IDSequenceGenerator.getNextID("Level");
			this.level = level;
			this.experience = experience;
		}
	}

	@SuppressWarnings("unused")
	private int getId() {
		return id;
	}

	public int getLevel() {
		synchronized (this) {
			return level;
		}
	}

	public void setLevel(int level) {
		synchronized (this) {
			if (level < 1) {
				level = 1;
			} else if (level > experienceList.length) {
				level = experienceList.length;
			}
			this.level = level;
			experience = 0;
		}
	}

	public int getExperience() {
		synchronized (this) {
			return experience;
		}
	}

	public void setExperience(int experience) {
		synchronized (this) {
			if (experience > getExperienceForLevel(level + 1)) {
				experience = getExperienceForLevel(level + 1) - 1;
			}
			this.experience = experience;
		}
	}

	public int getMaxStat() {
		synchronized (this) {
			return baseStat + (statPerLevel * level);
		}
	}

	private boolean canLevelUp() {
		synchronized (this) {
			if (level < experienceList.length) {
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean canLevelDown() {
		synchronized (this) {
			if (level > 1) {
				return true;
			} else {
				return false;
			}
		}
	}

	private int getExperienceForLevel(int level) {
		synchronized (this) {
			if (level > experienceList.length) {
				level = experienceList.length;
			}
			return experienceList[level + 1];
		}
	}

	private int experienceToLevelUp() {
		synchronized (this) {
			return getExperienceForLevel(level + 1) - experience;
		}
	}

	/**
	 *
	 * @param experience
	 * @return did experience change result in level change
	 * @throws LevelExceptionAlreadyMax
	 */
	public boolean addExperience(int experience) throws LevelExceptionAlreadyMax {
		synchronized (this) {
			if (experience >= experienceToLevelUp()) {
				if (canLevelUp()) {
					addLevel();
					experience = experience - experienceToLevelUp();
					if (experience > 0) {
						try {
							addExperience(experience);
						} catch (LevelExceptionAlreadyMax e) {
						}
					}
					return true;
				} else {
					this.experience = (this.experience + experienceToLevelUp()) - 1;
					throw new LevelExceptionAlreadyMax("Level: Cannot increase level when already maximum level.");
				}
			} else {
				this.experience = this.experience + experience;
				return false;
			}
		}
	}

	/**
	 *
	 * @param experience
	 * @return did experience change result in level change
	 * @throws LevelExceptionAlreadyMin
	 */
	public boolean removeExperience(int experience) throws LevelExceptionAlreadyMin {
		synchronized (this) {
			if (experience >= (this.experience + 1)) {
				if (canLevelDown()) {
					removeLevel();
					experience = experience - (this.experience + 1);
					this.experience = (getExperienceForLevel(level) - 1);
					if (experience > 0) {
						try {
							removeExperience(experience);
						} catch (LevelExceptionAlreadyMin e) {
						}
					}
					return true;
				} else {
					this.experience = 0;
					throw new LevelExceptionAlreadyMin("Level: Cannot increase level when already maximum level.");
				}
			} else {
				this.experience = this.experience - experience;
				return false;
			}
		}
	}

	public void addLevel() throws LevelExceptionAlreadyMax {
		synchronized (this) {
			if (canLevelUp()) {
				setLevel(level + 1);
			} else {
				throw new LevelExceptionAlreadyMax("Level: Cannot increase level when already maximum level.");
			}
		}
	}

	public void removeLevel() throws LevelExceptionAlreadyMin {
		synchronized (this) {
			if (canLevelDown()) {
				setLevel(level - 1);
			} else {
				throw new LevelExceptionAlreadyMin("Level: Cannot reduce level when already minimum level.");
			}
		}
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
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = (prime * result) + getId();
		return result;
	}

	/**
	 * Custom implementation requires for proper JPA/Hibernate function.
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
		Coordinate other = (Coordinate) obj;
		if (getId() == other.getId())
			return true;
		return false;
	}
}
