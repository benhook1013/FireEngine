package fireengine.client_io;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.logging.Level;

import fireengine.client_io.exception.ClientConnectionException;
import fireengine.main.FireEngineMain;
import fireengine.session.Session;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    ClientConnectionTelnet.java
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

public class ClientConnectionTelnet implements ClientConnection {
	private ClientConnectionTelnet ccon;
	private ClientIOTelnet telnet;
	private final SocketChannel sc;
	private String address;

	private boolean closing;
	private ArrayList<ByteBuffer> sendList;
	private final int SEND_LIMIT = 1000;
	private ArrayList<String> recieveList;
	private final int RECIEVE_LIMIT = 1000;
	private volatile boolean acceptInput;
	private StringBuilder sb = new StringBuilder();

	private Session sess;

	private static final String colourPrefix = "\u001B[";
	private static final String colourSeperator = ";";
	private static final String colourSuffix = "m";
	private static final String colourReset = "\u001B[0m";
	private static final String EOL = "\r\n";

	public ClientConnectionTelnet(ClientIOTelnet telnet, SocketChannel sc) {
		synchronized (this) {
			MyLogger.log(Level.INFO, "ClientConnectionTelnet: Telnet_IO_Connection created!");
			ccon = this;
			this.telnet = telnet;
			this.sc = sc;
			closing = false;
			acceptInput = false;
		}
	}

	@Override
	public void setupConnection(Session sess) throws ClientConnectionException {
		synchronized (this) {
			this.sess = sess;
			try {
				// Set SocketChannel flag to keep connections alive.
				ccon.sc.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			} catch (IOException e) {
				throw new ClientConnectionException("ClientConnectionTelnet: Failed to set SO_KEEPALIVE.", e);
			}
			try {
				ccon.address = ccon.sc.getLocalAddress().toString();
			} catch (IOException e) {
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Failed to get address for SocketChannel.", e);
				ccon.address = "error retrieving address";
			}
			ccon.sendList = new ArrayList<>();
			ccon.recieveList = new ArrayList<>();
			MyLogger.log(Level.INFO, "ClientConnectionTelnet: Telnet_IO_Connection set up: '" + address + "'.");
		}
	}

	public SocketChannel getSc() {
		return this.sc;
	}

	@Override
	// Write to connection from the game.
	public void writeToConnection(ClientConnectionOutput output, boolean ansi) {
		synchronized (sendList) {
			while ((!(sendList.size() >= SEND_LIMIT)) && output.hasNextLine()) {
				String string = parseOutput(output, ansi);
				sendList.add(ByteBuffer.wrap(string.getBytes()));
				output.nextLine();
			}
			telnet.addKeyQueue(ccon, SelectionKey.OP_WRITE, true);
		}
	}

	private String parseOutput(ClientConnectionOutput output, boolean ansi) {
		String string = "";
		while (output.hasNextPart()) {
			String addText = output.getText();
			ClientIOColour.COLOURS colourFG = output.getColourFG();
			ClientIOColour.COLOURS colourBG = output.getColourBG();
			output.nextPart();

			if (ansi) {
				if (colourFG != null) {
					string = string + colourPrefix + parseOutputColour(colourFG, true);
					if (colourBG != null) {
						string = string + colourSeperator + parseOutputColour(colourBG, false) + colourSuffix;
						addText = addText + colourReset;
					} else {
						// string = string + colourSeperator +
						// parseOutputColour(ClientIOColour.COLOURS.BLACK)
						// + colourSuffix;
						string = string + colourSuffix;
						addText = addText + colourReset;
					}
				} else if (colourBG != null) {
					string = string + colourPrefix + parseOutputColour(colourBG, false) + colourSuffix;
					addText = addText + colourReset;
				}
			}
			string = string + addText;
		}
		System.out.println("parseOutput output: '" + string + "'");
		string = string + EOL;
		return string;
	}

	private String parseOutputColour(ClientIOColour.COLOURS colour, boolean isFG) {
		String code = null;

		switch (colour) {
		case BLACK: {
			if (isFG) {
				code = "30";
			} else {
				code = "40";
			}
			break;
		}
		case RED: {
			if (isFG) {
				code = "31";
			} else {
				code = "41";
			}
			break;
		}
		case GREEN: {
			if (isFG) {
				code = "32";
			} else {
				code = "42";
			}
			break;
		}
		case YELLOW: {
			if (isFG) {
				code = "33";
			} else {
				code = "43";
			}
			break;
		}
		case BLUE: {
			if (isFG) {
				code = "34";
			} else {
				code = "44";
			}
			break;
		}
		case MAGENTA: {
			if (isFG) {
				code = "35";
			} else {
				code = "45";
			}
			break;
		}
		case CYAN: {
			if (isFG) {
				code = "36";
			} else {
				code = "46";
			}
			break;
		}
		case WHITE: {
			if (isFG) {
				code = "37";
			} else {
				code = "47";
			}
			break;
		}
		case BRIGHTBLACK: {
			if (isFG) {
				code = "30" + colourSeperator + "1";
			} else {
				code = "40";
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Tried to call Bright colour on Background.");
			}
			break;
		}
		case BRIGHTRED: {
			if (isFG) {
				code = "31" + colourSeperator + "1";
			} else {
				code = "41";
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Tried to call Bright colour on Background.");
			}
			break;
		}
		case BRIGHTGREEN: {
			if (isFG) {
				code = "32" + colourSeperator + "1";
			} else {
				code = "42";
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Tried to call Bright colour on Background.");
			}
			break;
		}
		case BRIGHTYELLOW: {
			if (isFG) {
				code = "33" + colourSeperator + "1";
			} else {
				code = "43";
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Tried to call Bright colour on Background.");
			}
			break;
		}
		case BRIGHTBLUE: {
			if (isFG) {
				code = "34" + colourSeperator + "1";
			} else {
				code = "44";
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Tried to call Bright colour on Background.");
			}
			break;
		}
		case BRIGHTMAGENTA: {
			if (isFG) {
				code = "35" + colourSeperator + "1";
			} else {
				code = "45";
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Tried to call Bright colour on Background.");
			}
			break;
		}
		case BRIGHTCYAN: {
			if (isFG) {
				code = "36" + colourSeperator + "1";
			} else {
				code = "46";
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Tried to call Bright colour on Background.");
			}
			break;
		}
		case BRIGHTWHITE: {
			if (isFG) {
				code = "37" + colourSeperator + "1";
			} else {
				code = "47";
				MyLogger.log(Level.WARNING, "ClientConnectionTelnet: Tried to call Bright colour on Background.");
			}
			break;
		}
		}

		return code;
	}

	public ByteBuffer writeFromConnection() {
		synchronized (sendList) {
			if (!sendList.isEmpty()) {
				return sendList.get(0);
			} else {
				if (closing) {
					// Register with no SelectionKey as only want to finish
					// writing; wont stop registering for WRITE to finish writes
					// later.
					telnet.addKeyQueue(ccon, 0, false);
				} else {
					telnet.addKeyQueue(ccon, SelectionKey.OP_READ, false);
				}
				return null;
			}
		}
	}

	public void finishedWrite() {
		synchronized (sendList) {
			sendList.remove(0);

			if (closing) {
				if (sendList.isEmpty()) {
					sess.notifyCconFinished();
				}
			}
		}
	}

	@Override
	public void acceptInput() {
		acceptInput = true;
		// TODO Unsure if need this
		// telnet.addKeyQueue(ccon, SelectionKey.OP_READ, true);
	}

	/**
	 * Clear current input and refuse further input.
	 */
	@Override
	public void refuseInput() {
		synchronized (recieveList) {
			acceptInput = false;
			recieveList.clear();
		}
	}

	// Read to connection from client IO.
	public void readToConnection(String string) {
		if (string.length() > FireEngineMain.CLIENT_IO_INPUT_MAX_LENGTH) {
			MyLogger.log(Level.WARNING,
					"ClientConnectionTelnet: Input recieved exceeded maximum input length; input dropped.");
			return;
		}

		synchronized (recieveList) {

			if (!acceptInput) {
				return;
			}

			System.out.println("readToConnection: '" + string + "'");
			if (!(recieveList.size() >= RECIEVE_LIMIT)) {
				recieveList.add(string);
			}
		}
		sess.notifyInput();
	}

	// Read input to Connection for combination and parsing.
	public void readToConnectionPart(char c) {
		if ((c == '\r') | (c == '\n')) {
			// System.out.println("EOL detected.");
			// System.out.println(sb.toString());

			if (sb.length() > 0) {
				readToConnection(sb.toString());
				sb = new StringBuilder();
			}
		} else if (c == '\b') {
			if (sb.length() > 0) {
				sb.replace(sb.length() - 1, sb.length(), "");
			}
		} else {
			sb.append(c);
		}

	}

	@Override
	public String readFromConnection() {
		synchronized (recieveList) {
			if (!recieveList.isEmpty()) {
				return recieveList.remove(0);
			} else {
				return null;
			}
		}
	}

	@Override
	public void shutdown() {
		closing = true;
	}

	@Override
	public void close() {
		synchronized (this) {
			if (sc.isOpen()) {
				try {
					// MyLogger.log(Level.INFO, "ClientConnectionTelnet: ClientConnectionTelnet
					// shutdown: '" + sc.getLocalAddress().toString() + "'(local).");
					MyLogger.log(Level.INFO, "ClientConnectionTelnet: ClientConnectionTelnet shutdown: '"
							+ sc.getRemoteAddress().toString() + "'(remote).");
					sc.close();
				} catch (IOException e) {
					MyLogger.log(Level.WARNING,
							"ClientConnectionTelnet: IOException while trying to close ClientConnectionTelnet.", e);
				}
			}
		}
	}

}
