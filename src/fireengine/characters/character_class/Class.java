package fireengine.characters.character_class;

import java.util.ArrayList;

import fireengine.characters.character_class.skillsets.Base_Skillset;

/*
 *    Copyright 2017 Ben Hook
 *    Class.java
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

public abstract class Class {
	protected String className;

	protected ArrayList<Base_Skillset> skillsetList;

	public String getClassName() {
		return className;
	}

	public void setClassName(String name) {
		this.className = name;
	}

	protected void addSkillset(Base_Skillset skillset) {
		skillsetList.add(skillset);
	}
}
