package fireengine.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/*
 *    Copyright 2017 Ben Hook
 *    ConfigLoader.java
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

/**
 * A class to load settings from text file.
 * 
 * @author Ben Hook
 */
public class ConfigLoader {
	static Properties config;

	private ConfigLoader() {
	}

	/**
	 * File lives at top level of project (TODO check where on built app).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void loadSettings(String filePath) throws FileNotFoundException, IOException {
		config = new Properties();
		File configFile = new File(filePath);
		FileInputStream configFileInputStream = new FileInputStream(configFile);

		config.load(configFileInputStream);

		configFileInputStream.close();
	}

	public static String getSetting(String name) {
		return config.getProperty(name);
	}
}
