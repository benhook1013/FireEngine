package fireengine.character.condition;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import fireengine.gameworld.map.room.Room;
import fireengine.util.IDSequenceGenerator;

/*
 *    Copyright 2019 Ben Hook
 *    ConditionPlayer.java
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

// TODO Do not need separate condition class for PC vs NPC
@Entity
@Table(name = "CONDITION_PLAYER")
public class ConditionPlayer extends Condition {
	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "LEVEL", nullable = false)
	@NotNull
	private Level level;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "HEALTH", nullable = false)
	@NotNull
	private Health health;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "MANA", nullable = false)
	@NotNull
	private Mana mana;

	@SuppressWarnings("unused")
	private ConditionPlayer() {
	}

	public ConditionPlayer(Boolean bool) {
		id = IDSequenceGenerator.getNextID("ConditionPlayer");
		setLevel(new Level(1));
		setHealth(new Health(this.level.getMaxStat()));
		setMana(new Mana(this.level.getMaxStat()));
	}

	private int getId() {
		return id;
	}

	private void setLevel(Level level) {
		this.level = level;
	}

	private Level getLevel() {
		return level;
	}

	@Override
	public int getLevelNumber() {
		return level.getLevel();
	}

	public void setLevelNumber(int level) {
		this.level.setLevel(level);
	}

	public int getExperience() {
		return level.getExperience();
	}

	public void setExperience(int experience) {
		level.setExperience(experience);
	}

	@SuppressWarnings("unused")
	private Health getHealth() {
		return health;
	}

	private void setHealth(Health health) {
		this.health = health;
	}

	@SuppressWarnings("unused")
	private Mana getMana() {
		return mana;
	}

	private void setMana(Mana mana) {
		this.mana = mana;
	}

	@Override
	public int getMaxHealth() {
		return getLevel().getMaxStat();
	}

	@Override
	public int getCurrentHealth() {
		return health.getHealth(getLevel().getMaxStat());
	}

	@Override
	public int getMaxMana() {
		return getLevel().getMaxStat();
	}

	@Override
	public int getCurrentMana() {
		return mana.getMana(getLevel().getMaxStat());
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
		ConditionPlayer other = (ConditionPlayer) obj;
		if (getId() == other.getId())
			return true;
		return false;
	}
}
