package fireengine.character.condition;

/*
 *    Copyright 2019 Ben Hook
 *    Condition.java
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

public abstract class Condition {
	protected Condition() {
	}

	public abstract int getLevelNumber();

	public abstract int getMaxHealth();

	public abstract int getCurrentHealth();

	public abstract int getMaxMana();

	public abstract int getCurrentMana();
}
