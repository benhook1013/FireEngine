package com.github.benhook1013.fireengine.characters.character_class;

import java.util.ArrayList;
import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.github.benhook1013.fireengine.characters.character_class.skillsets.Skillset;
import com.github.benhook1013.fireengine.utils.ConfigLoader;
import com.github.benhook1013.fireengine.utils.MyClassLoader;

/*
 *    Copyright 2017 Ben Hook
 *    CharacterClass.java
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
@Table(name = "CHARACTER_CLASS")
public class CharacterClass {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CHAR_CLASS_ID")
	private int id;

	@Column(name = "CHAR_CLASS_NAME")
	protected String className;

	@Transient
	private static MyClassLoader classLoader;

	@Transient
	protected static ArrayList<Class<Skillset>> skillsetClassList;

	@Transient
	protected ArrayList<Skillset> skillsetList;

	@SuppressWarnings("unchecked")
	public static void loadSkillsets() throws ClassNotFoundException {
		classLoader = new MyClassLoader();

		String skillsetNameString = ConfigLoader.getSetting("charClassSkillsetList");
		ArrayList<String> skillsetNameArray = new ArrayList<>();
		Collections.addAll(skillsetNameArray, skillsetNameString.split(";"));

		skillsetClassList = new ArrayList<>();
		for (String skillsetName : skillsetNameArray) {
			System.out.println("skillsetName: '" + skillsetName + "'");
			skillsetClassList.add((Class<Skillset>) classLoader.loadClass(skillsetName));
		}
	}

	public CharacterClass() {
		className = "Novice";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String name) {
		this.className = name;
	}

	protected void addSkillset(Skillset skillset) {
		skillsetList.add(skillset);
	}
}
