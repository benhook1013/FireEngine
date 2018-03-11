package fireengine.characters.character_class.skillsets;

import java.util.ArrayList;

/*
 *    Copyright 2017 Ben Hook
 *    Fighter.java
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

public class Base_Skillset {
	protected String skillsetName;
	private int skillsetExperience;

	protected ArrayList<Base_Skill> skillList;

	protected class Base_Skill {
		protected String skillName;
	}

	public Base_Skillset() {
		setSkillsetExperience(0);
	}

	public String getSkillsetName() {
		return skillsetName;
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
