package fireengine.util;

/*
 *    Copyright 2019 Ben Hook
 *    MyClassLoader.java
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
	private Loader loader;

	public MyClassLoader() {
		loader = new Loader(this.getClass().getClassLoader());
	}

	private class Loader extends ClassLoader {

		public Loader(ClassLoader parent) {
			super(parent);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return super.loadClass(name);
		}

	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loader.loadClass(name);
	}
}