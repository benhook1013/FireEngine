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
		logLevel = Level.FINER;

		try {
//			LOGGER.setUseParentHandlers(false);

//			consoleHandler = new ConsoleHandler();
//			fileHandler = new FileHandler("Log");

//			LOGGER.addHandler(fileHandler);

//			consoleHandler.setLevel(Level.INFO);
//			fileHandler.setLevel(Level.ALL);
			LOGGER.setLevel(logLevel);
//			LOGGER.getParent().setLevel(Level.FINEST);

			MyLoggerFormatter formatter = new MyLoggerFormatter();
//			consoleHandler.setFormatter(formatter);

//			LOGGER.addHandler(consoleHandler);

			for (Handler handler : LOGGER.getParent().getHandlers()) {
				handler.setFormatter(formatter);
				handler.setLevel(logLevel);
			}

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

		public static final String ANSI_BRIGHT_BLACK = "\u001b[30;1m";
		public static final String ANSI_BRIGHT_RED = "\u001b[31;1m";
		public static final String ANSI_BRIGHT_GREEN = "\u001b[32;1m";
		public static final String ANSI_BRIGHT_YELLOW = "\u001b[33;1m";
		public static final String ANSI_BRIGHT_BLUE = "\u001b[34;1m";
		public static final String ANSI_BRIGHT_MAGENTA = "\u001b[35;1m";
		public static final String ANSI_BRIGHT_CYAN = "\u001b[36;1m";
		public static final String ANSI_BRIGHT_WHITE = "\u001b[37;1m";

		public static final String ANSI_BACKGROUND_BLACK = "\u001b[40m";
		public static final String ANSI_BACKGROUND_RED = "\u001B[41m";
		public static final String ANSI_BACKGROUND_GREEN = "\u001B[42m";
		public static final String ANSI_BACKGROUND_YELLOW = "\u001B[43m";
		public static final String ANSI_BACKGROUND_BLUE = "\u001B[44m";
		public static final String ANSI_BACKGROUND_MAGENTA = "\u001B[45m";
		public static final String ANSI_BACKGROUND_CYAN = "\u001B[46m";
		public static final String ANSI_BACKGROUND_WHITE = "\u001B[47m";

		public static final String ANSI_BACKGROUND_BRIGHT_BLACK = "\u001b[40;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_RED = "\u001b[41;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_GREEN = "\u001b[42;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_YELLOW = "\u001b[43;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_BLUE = "\u001b[44;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_MAGENTA = "\u001b[45;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_CYAN = "\u001b[46;1m";
		public static final String ANSI_BACKGROUND_BRIGHT_WHITE = "\u001b[47;1m";

		// Here you can configure the format of the output and
		// its colour by using the ANSI escape codes defined above.

		// format is called for every console log message
		@Override
		public String format(LogRecord record) {
			// This example will print date/time, class, and log level in yellow,
			// followed by the log message and it's parameters in white .
			StringBuilder builder = new StringBuilder();

			if (record.getLevel().intValue() == Level.SEVERE.intValue()) {
				builder.append(ANSI_BRIGHT_RED);
			} else if (record.getLevel().intValue() == Level.WARNING.intValue()) {
				builder.append(ANSI_BRIGHT_MAGENTA);
			} else if (record.getLevel().intValue() == Level.INFO.intValue()) {
				builder.append(ANSI_BLACK);
			} else {
				builder.append(ANSI_YELLOW);
			}

			builder.append("[");
			builder.append(calcDate(record.getMillis()));
			builder.append("]");

			builder.append(" [");
			// Below if usual way of getting log name, results in:
			// [fireengine.util.MyLogger log]
//			if (record.getSourceClassName() != null) {
//				builder.append(record.getSourceClassName());
//				if (record.getSourceMethodName() != null) {
//					builder.append(" " + record.getSourceMethodName());
//				}
//			} else {
//				builder.append(record.getLoggerName());
//			}
			builder.append("fireengine log");
			builder.append("]");

			builder.append(" [");
			builder.append(record.getLevel().getName());
			builder.append("]");

//			builder.append(ANSI_RESET);
//			builder.append(ANSI_BLACK);
			builder.append(" - ");
//			builder.append(record.getMessage());
			builder.append(formatMessage(record));

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
				pw.println();
				record.getThrown().printStackTrace(pw);
				pw.close();
				builder.append(sw.toString());
			}

			builder.append(ANSI_RESET);
			builder.append("\n");

			return builder.toString();
		}

		private String calcDate(long millisecs) {
			SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date resultdate = new Date(millisecs);
			return date_format.format(resultdate);
		}
	}
}
