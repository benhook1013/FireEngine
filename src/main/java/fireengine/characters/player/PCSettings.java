package fireengine.characters.player;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*
 *    Copyright 2017 Ben Hook
 *    PCSettings.java
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
@Table(name = "PC_SETTINGS")
public class PCSettings {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PC_SETTINGS_ID")
	private int id;

	@Column(name = "PC_SETTINGS_MAP_EDITOR")
	private boolean mapEditor;

	public PCSettings() {
		mapEditor = false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isMapEditor() {
		return mapEditor;
	}

	public void setMapEditor(boolean mapEditor) {
		this.mapEditor = mapEditor;
	}
}
