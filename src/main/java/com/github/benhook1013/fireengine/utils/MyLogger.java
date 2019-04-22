package com.github.benhook1013.fireengine.utils;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 *    Copyright 2017 Ben Hook
 *    MyLogger.java
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
 * Wrapper class for custom implementation of {@link Logger}.
 * 
 * @author Ben Hook
 */
public class MyLogger {
	static final Logger LOGGER = Logger.getLogger("fireengine");
	static final MyLogger instance = new MyLogger();

	Handler consoleHandler;
	Handler fileHandler;

	private MyLogger() {
		try {
			// consoleHandler = new ConsoleHandler();
			// fileHandler = new FileHandler("Log");

			// LOGGER.addHandler(consoleHandler);
			// LOGGER.addHandler(fileHandler);

			// consoleHandler.setLevel(Level.ALL);
			// fileHandler.setLevel(Level.ALL);
			LOGGER.setLevel(Level.ALL);

			LOGGER.log(Level.CONFIG, "Logger configuration completed successfully.");
		} catch (SecurityException e) {
			LOGGER.log(Level.SEVERE, "Failed to initiate handlers for Logger in MyLogger.", e);
		}
	}

	public static void log(Level level, String msg) {
		LOGGER.log(level, msg);
	}

	public static void log(Level level, String msg, Throwable thrown) {
		LOGGER.log(level, msg, thrown);
	}
}
