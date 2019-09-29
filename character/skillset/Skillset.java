package fireengine.character.skillset;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import fireengine.character.Character;
import fireengine.character.command.action.Action;
import fireengine.character.skillset.Skillset.SkillsetCategory.ActionEntry;
import fireengine.character.skillset.exception.SkillsetExceptionLackExperience;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.room.Room;
import fireengine.util.IDSequenceGenerator;

/*
 *    Copyright 2019 Ben Hook
 *    Skillset.java
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
@Table(name = "SKILLSET")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Skillset implements Comparable<Skillset> {
	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

//	@Column(name = "NAME", nullable = false)
//	@NotNull
//	private String name;

	@Column(name = "EXPERIENCE", nullable = false)
	@NotNull
	private int skillsetExperience;

	@Transient
	private ArrayList<SkillsetCategory> categoryList;

	protected Skillset() {
		categoryList = new ArrayList<Skillset.SkillsetCategory>();
	}

	public Skillset(Boolean bool) {
		this();
		id = IDSequenceGenerator.getNextID("Skillset");
		setSkillsetExperience(0);
	}

//	protected Skillset(String name) {
//		this();
//		this.name = name;
//	}

	public int getId() {
		return id;
	}

	/**
	 * Static method that requires overriding. Workaround to be able to require
	 * subclass to implement static method.
	 * 
	 * @return nothing as this should not be called. Overrided method will return
	 *         skillset name.
	 */
	public String getName() {
		return "SKILLSET NAME NOT SET";
	}

	public int getSkillsetExperience() {
		return skillsetExperience;
	}

	public void setSkillsetExperience(int experience) {
		skillsetExperience = experience;
	}

	public void addCategory(SkillsetCategory category) {
		categoryList.add(category);
	}

	public List<ActionEntry> getSkillEntries() {
		List<ActionEntry> skillEntrySet = new ArrayList<ActionEntry>();

		for (SkillsetCategory category : categoryList) {
			skillEntrySet.addAll(category.getSkillEntries());
		}

		return skillEntrySet;
	}

	/**
	 * This class will actually contain the Actions for the Skillset. This is purely
	 * for the ability to present actions/skills with grouped headings in the
	 * Skillset's AB list.
	 * 
	 * @author Ben Hook
	 */
	public class SkillsetCategory {
		String name;
		private ArrayList<ActionEntry> skillList = new ArrayList<ActionEntry>();

		public SkillsetCategory(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public class ActionEntry {
			private int requiredExperience;
			private Action action;

			ActionEntry(int requiredExperience, Action action) {
				this.requiredExperience = requiredExperience;
				this.action = action;
			}

			public ClientConnectionOutput doAction(Character character, Matcher matcher)
					throws SkillsetExceptionLackExperience {
				if (skillsetExperience < requiredExperience) {
					throw new SkillsetExceptionLackExperience(
							"ActionEntry: Character lacks Skillset experience to use skill/Action.");
				} else {
					return action.callAction(character, matcher);
				}
			}

			public Action getAction() {
				return action;
			}
		}

		public void addSkillEntry(int requiredExperience, Action action) {
			skillList.add(new ActionEntry(requiredExperience, action));
		}

		public ArrayList<ActionEntry> getSkillEntries() {
			return skillList;
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
		Skillset other = (Skillset) obj;
		if (getId() == other.getId())
			return true;
		return false;
	}

	@Override
	public int compareTo(Skillset other) {
		// compareTo should return < 0 if this is supposed to be
		// less than other, > 0 if this is supposed to be greater than
		// other and 0 if they are supposed to be equal
		if (other instanceof General) {
			return 1;
		}
		return this.getName().compareToIgnoreCase(other.getName());
	}
}
