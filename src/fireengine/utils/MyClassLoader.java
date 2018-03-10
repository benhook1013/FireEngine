package fireengine.utils;

import fireengine.session.phase.PhaseInterface;

/*
 *    Copyright 2017 Ben Hook
 *    PhaseWelcome.java
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

public class MyClassLoader {
	private Loader loader = new Loader(MyClassLoader.class.getClassLoader());

	private class Loader extends ClassLoader {

		public Loader(ClassLoader parent) {
			super(parent);
		}

		@SuppressWarnings("unchecked")
		public Class<PhaseInterface> loadClass(String name) throws ClassNotFoundException {
			return (Class<PhaseInterface>) super.loadClass(name);
		}

	}

	public Class<PhaseInterface> loadClass(String name) throws ClassNotFoundException {
		return (Class<PhaseInterface>) loader.loadClass(name);
	}
}