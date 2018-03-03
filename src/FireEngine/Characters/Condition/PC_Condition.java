package FireEngine.Characters.Condition;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/*
 *    Copyright 2017 Ben Hook
 *    PC_Condition.java
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
@Table(name = "PC_CONDITION")
public class PC_Condition extends Base_Condition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PC_COND_ID")
	private int id;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PC_COND_CHAR_LEVEL_ID")
	private Level level;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PC_COND_CHAR_HEALTH_ID")
	private Health health;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PC_COND_CHAR_MANA_ID")
	private Mana mana;

	private PC_Condition() {
		super();
	}

	public PC_Condition(int level) {
		this();
		setLevel(new Level(level));
		setHealth(new Health(this.level.getMaxStat()));
		setMana(new Mana(this.level.getMaxStat()));
	}

	@SuppressWarnings("unused")
	private int getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
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
}
