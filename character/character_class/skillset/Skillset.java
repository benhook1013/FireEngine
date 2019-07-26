package fireengine.character.character_class.skillset;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

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
public abstract class Skillset {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SKILLSET_ID")
	@NotNull
	private int id;

	@Column(name = "SKILLSET_NAME")
	@NotNull
	protected String skillsetName;
	
	@Column(name = "SKILLSET_SKILLSET_EXPERIENCE")
	@NotNull
	private int skillsetExperience;

	@Transient
	protected ArrayList<Skill> skillList;

	protected class Skill {
		protected String skillName;
	}

	protected Skillset() {
		setSkillsetExperience(0);
	}

	protected Skillset(String skillsetName) {
		this();
		this.skillsetName = skillsetName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSkillsetName() {
		return skillsetName;
	}

	public void setSkillsetName(String skillsetName) {
		this.skillsetName = skillsetName;
	}

	public int getSkillsetExperience() {
		return skillsetExperience;
	}

	public void setSkillsetExperience(int experience) {
		skillsetExperience = experience;
	}

	protected void addSkill(Skill skill) {
		skillList.add(skill);
	}

	protected ArrayList<Skill> getSkillList() {
		return skillList;
	}
}
