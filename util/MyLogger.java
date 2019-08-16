package fireengine.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/*
 *    Copyright 2019 Ben Hook
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
 * TODO Clean this call up once we are comfortable that its acting correctly.
 *
 * @author Ben Hook
 */
public class MyLogger {
	static final Logger LOGGER = Logger.getLogger("fireengine");
	static final MyLogger instance = new MyLogger();

	Handler consoleHandler;
	Handler fileHandler;
	Level logLevel;

	private MyLogger() {
		// Initialise logger level to config to show config message below before it can
		// be set.
		try {
			logLevel = Level.CONFIG;
			// TODO Enable and configure file handler.

//			fileHandler = new FileHandler("Log");

//			LOGGER.addHandler(fileHandler);

//			fileHandler.setLevel(Level.ALL);
			LOGGER.setLevel(logLevel);

			MyLoggerFormatter formatter = new MyLoggerFormatter();

			for (Handler handler : LOGGER.getParent().getHandlers()) {
				handler.setFormatter(formatter);
				handler.setLevel(logLevel);
			}

			LOGGER.log(Level.CONFIG,
					String.format("MyLogger: Logger formatter set and Level initialised to %s.", logLevel.toString()));
		} catch (SecurityException e) {
			LOGGER.log(Level.SEVERE, "MyLogger: Failed to set formatter or initialise logger level.", e);
		}

		String configFileLoggerLevel = ConfigLoader.getSetting("loggerLevel");

		if (configFileLoggerLevel == null) {
			logLevel = Level.INFO;
			MyLogger.log(Level.SEVERE, "MyLogger: loggerLevel not found in server config file, defaulting to INFO.");
		}

		switch (configFileLoggerLevel) {
		case "ALL": {
			logLevel = Level.ALL;
			break;
		}
		case "CONFIG": {
			logLevel = Level.CONFIG;
			break;
		}
		case "FINE": {
			logLevel = Level.FINE;
			break;
		}
		case "FINER": {
			logLevel = Level.FINER;
			break;
		}
		case "FINEST": {
			logLevel = Level.FINEST;
			break;
		}
		case "INFO": {
			logLevel = Level.INFO;
			break;
		}
		case "OFF": {
			logLevel = Level.OFF;
			break;
		}
		case "SEVERE": {
			logLevel = Level.SEVERE;
			break;
		}
		case "WARNING": {
			logLevel = Level.WARNING;
			break;
		}
		default: {
			logLevel = Level.INFO;
			MyLogger.log(Level.SEVERE,
					String.format("MyLogger: Could not parse configFileLoggerLevel '%s', defaulting to INFO.",
							configFileLoggerLevel));
			break;
		}
		}

		try {
//			fileHandler.setLevel(Level.ALL);
			MyLogger.log(Level.FINE, String.format("MyLogger: Setting log level to %s.", logLevel.toString()));
			LOGGER.setLevel(logLevel);

			for (Handler handler : LOGGER.getParent().getHandlers()) {
				handler.setLevel(logLevel);
			}

			LOGGER.log(Level.INFO, String.format("MyLogger: Logger Level set to %s.", logLevel.toString()));
		} catch (SecurityException e) {
			LOGGER.log(Level.SEVERE, "MyLogger: Failed to set Level.", e);
		}
	}

	private static String stripColour(String text) {
		Pattern p = Pattern.compile("\u001B\\[[\\d;]*[^\\d;]", Pattern.DOTALL);
		return p.matcher(text).replaceAll("");
	}

	public static void log(Level level, String msg) {
		LOGGER.log(level, stripColour(msg));
	}

	public static void log(Level level, String msg, Throwable thrown) {
		LOGGER.log(level, stripColour(msg), thrown);
	}

	public class MyLoggerFormatter extends Formatter {
		// ANSI escape code
		public static final String ANSI_RESET = "\u001B[0m";

		public static final String ANSI_BLACK = "\u001B[30m";
		public static final String ANSI_RED = "\u001B[31m";
		public static final String ANSI_GREEN = "\u001B[32m";
		public static final String ANSI_YELLOW = "\u001B[33m";
		public static final String ANSI_BLUE = "\u001B[34m";
		public static final String ANSI_MAGENTA = "\u001B[35m";
		public static final String ANSI_CYAN = "\u001B[36m";
		public static final String ANSI_WHITE = "\u001B[37m";

		public static final String ANSI_BRIGHT_BLACK = "\u001B[30;1m";
		public static final String ANSI_BRIGHT_RED = "\u001B[31;1m";
		public static final String ANSI_BRIGHT_GREEN = "\u001B[32;1m";
		public static final String ANSI_BRIGHT_YELLOW = "\u001B[33;1m";
		public static final String ANSI_BRIGHT_BLUE = "\u001B[34;1m";
		public static final String ANSI_BRIGHT_MAGENTA = "\u001B[35;1m";
		public static final String ANSI_BRIGHT_CYAN = "\u001B[36;1m";
		public static final String ANSI_BRIGHT_WHITE = "\u001B[37;1m";

		public static final String ANSI_BACKGROUND_BLACK = "\u001B[40m";
		public static final String ANSI_BACKGROUND_RED = "\u001B[41m";
		public static final String ANSI_BACKGROUND_GREEN = "\u001B[42m";
		public static final String ANSI_BACKGROUND_YELLOW = "\u001B[43m";
		public static final String ANSI_BACKGROUND_BLUE = "\u001B[44m";
		public static final String ANSI_BACKGROUND_MAGENTA = "\u001B[45m";
		public static final String ANSI_BACKGROUND_CYAN = "\u001B[46m";
		public static final String ANSI_BACKGROUND_WHITE = "\u001B[47m";

		public static final String ANSI_BACKGROUND_BRIGHT_BLACK = "\u001B[40;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_RED = "\u001B[41;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_GREEN = "\u001B[42;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_YELLOW = "\u001B[43;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_BLUE = "\u001B[44;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_MAGENTA = "\u001B[45;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_CYAN = "\u001B[46;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_WHITE = "\u001B[47;1m";

		// Here you can configure the format of the output and
		// its colour by using the ANSI escape codes defined above.

		// format is called for every console log message
		@Override
		public String format(LogRecord record) {
			// This example will print date/time, class, and log level in yellow,
			// followed by the log message and it's parameters in white .
			StringBuilder builder = new StringBuilder();

			String firstLine = "";

			firstLine = firstLine + "[";
			firstLine = firstLine + calcDate(record.getMillis());
			firstLine = firstLine + "]";

			firstLine = firstLine + " [" + "fireengine log" + "]";

			firstLine = firstLine + " [" + record.getLevel().getName() + "]";

			firstLine = firstLine + " - ";
//			builder.append(record.getMessage());
			firstLine = firstLine + formatMessage(record);

			colourWrapString(builder, record, firstLine);
			builder.append("\n");

//			Object[] params = record.getParameters();
//
//			if (params != null) {
//				builder.append("\t");
//				for (int i = 0; i < params.length; i++) {
//					builder.append(params[i]);
//					if (i < params.length - 1)
//						builder.append(", ");
//				}
//			}
//			builder.append("\n");
//
//			if (record.getThrown() != null) {
//				for (StackTraceElement ste : record.getThrown().getStackTrace()) {
//					builder.append(ANSI_BRIGHT_RED);
//					builder.append(ste.toString() + "\n");
//				}
//			}

			// Everything prior to this is the first line such as:
			// [2019-07-30 21:56:31] [fireengine.util.MyLogger log] [SEVERE] - Session:
			// Unexpected exception caught.

			// The below will create the rest of the stack trace, as below:
//			java.lang.ArrayIndexOutOfBoundsException: -1
//			at fireengine.gameworld.map.GameMap.getRoom(GameMap.java:119)
//			at fireengine.gameworld.map.GameMap.createRoom(GameMap.java:181)
//			at fireengine.gameworld.map.GameMap.createRoom(GameMap.java:233)
//			at fireengine.character.command.character_comand.map_editor.CreateRoom.doAction(CreateRoom.java:51)
//			at fireengine.character.player.Player.acceptInput(Player.java:227)
//			at fireengine.character.player.Player.acceptInput(Player.java:214)
//			at mud_game.session.phase.PhaseInWorld.acceptInput(PhaseInWorld.java:54)
//			at fireengine.session.phase.PhaseManager.acceptInput(PhaseManager.java:81)
//			at fireengine.session.Session$2.call(Session.java:135)
//			at fireengine.session.Session$2.call(Session.java:128)
//			at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
//			at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1135)
//			at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
//			at java.base/java.lang.Thread.run(Thread.java:844)

			if (record.getThrown() != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				colourWrapString(builder, record, sw.toString());
			}

			return builder.toString();
		}

		private void colourWrapString(StringBuilder builder, LogRecord record, String text) {
			if (record.getLevel().intValue() == Level.SEVERE.intValue()) {
				builder.append(ANSI_BRIGHT_RED);
			} else if (record.getLevel().intValue() == Level.WARNING.intValue()) {
				builder.append(ANSI_BRIGHT_MAGENTA);
			} else if (record.getLevel().intValue() == Level.INFO.intValue()) {
				builder.append(ANSI_BLACK);
			} else {
				builder.append(ANSI_YELLOW);
			}
			builder.append(text);
			// TODO Remove end of line here, add reset and add EOL again to prevent reset on
			// newline
			builder.append(ANSI_RESET);
		}

		private String calcDate(long millisecs) {
			SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date resultdate = new Date(millisecs);
			return date_format.format(resultdate);
		}
	}
}