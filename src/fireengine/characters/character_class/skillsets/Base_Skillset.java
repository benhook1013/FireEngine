package fireengine.characters.character_class.skillsets;

import java.util.ArrayList;

import javax.persistence.*;

/*
 *    Copyright 2017 Ben Hook
 *    Base_Skillet.java
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
@Table(name = "BASE_SKILLSET")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Base_Skillset {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "BASE_SKILLSET_ID")
	private int id;

	@Column(name = "BASE_SKILLSET_NAME")
	protected String skillsetName;
	@Column(name = "BASE_SKILLSET_SKILLSET_EXPERIENCE")
	private int skillsetExperience;

	@Transient
	protected ArrayList<Base_Skill> skillList;

	protected class Base_Skill {
		protected String skillName;
	}

	protected Base_Skillset() {
		setSkillsetExperience(0);
	}

	protected Base_Skillset(String skillsetName) {
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

	protected void addSkill(Base_Skill skill) {
		skillList.add(skill);
	}

	protected ArrayList<Base_Skill> getSkillList() {
		return skillList;
	}
}
