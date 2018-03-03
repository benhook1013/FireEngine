package cyber_world.client_io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import cyber_world.client_io.exceptions.Client_IO_Telnet_Exception;
import cyber_world.session.Session;
import cyber_world.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    ClientIOTelnet.java
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
 * Workhorse of the telnet IO, a single thread that scales extremely well and
 * should be able to serve thousands of connections.
 * 
 * @author Ben Hook
 */
public class ClientIOTelnet extends Thread {
	private String address;
	private int port;
	private Selector sel;
	private ServerSocketChannel ssc;
	private static final int BUFFER_SIZE = 64; // The buffer into which we'll
												// read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private List<SelectItem> keyList;

	private volatile boolean running;
	private volatile boolean accepting;

	/**
	 * A small class used to contain info about pending {@link SelectionKey}
	 * changes.
	 * 
	 * @author Ben Hook
	 */
	private class SelectItem {
		private Client_Connection_Telnet ccon;
		private int key;

		public SelectItem(Client_Connection_Telnet ccon, int key) {
			this.ccon = ccon;
			this.key = key;
		}

		public int getKey() {
			return key;
		}

		public Client_Connection_Telnet getCcon() {
			return ccon;
		}
	}

	public ClientIOTelnet(String address, int port) throws Client_IO_Telnet_Exception {
		this.address = address;
		this.port = port;

		MyLogger.log(Level.INFO, "ClientIOTelnet: Instantiating ClientIOTelnet...");
		keyList = Collections.synchronizedList(new LinkedList<SelectItem>());
		try {
			sel = initSelector();
		} catch (Client_IO_Telnet_Exception e) {
			throw new Client_IO_Telnet_Exception(
					"ClientIOTelnet: Failed to initialise Selector while instantiating ClientIOTelnet.", e);
		}
	}

	/**
	 * Sets whether to accept new incoming connections or not,
	 * 
	 * @param accepting
	 */
	public void setAccepting(boolean accepting) {
		this.accepting = accepting;
	}

	/**
	 * Tries to open, configure and register the {@link Selector}, throwing
	 * {@link Client_IO_Telnet_Exception} upon exception.
	 * 
	 * @return {@link Selector}
	 * @throws Client_IO_Telnet_Exception
	 */
	private Selector initSelector() throws Client_IO_Telnet_Exception {
		try {
			// New Selector provided by OS.
			sel = Selector.open();
		} catch (IOException e) {
			throw new Client_IO_Telnet_Exception("ClientIOTelnet: ClientIOTelnet: Failed to open selector.", e);
		}

		try {
			// New ServerSocketChannel to accept connection requests.
			ssc = ServerSocketChannel.open();
		} catch (IOException e) {
			try {
				sel.close();
			} catch (IOException e2) {
				throw new Client_IO_Telnet_Exception(
						"ClientIOTelnet: Failed to close Selector while failed to open server ServerSocketChannel.",
						e2);
			}
			throw new Client_IO_Telnet_Exception("ClientIOTelnet: Failed to open server ServerSocketChannel.", e);
		}

		try {
			// Set ServerSocketChannel into non blocking mode, as NIO requires.
			ssc.configureBlocking(false);

		} catch (IOException e) {
			try {
				sel.close();
			} catch (IOException e2) {
				throw new Client_IO_Telnet_Exception(
						"ClientIOTelnet: Failed to close Selector while failed to configure blocking on server ServerSocketChannel.",
						e2);
			}
			try {
				ssc.close();
			} catch (IOException e3) {
				throw new Client_IO_Telnet_Exception(
						"ClientIOTelnet: Failed to close server ServerSocketChannel while failed to configure blocking on server ServerSocketChannel.",
						e3);
			}
			throw new Client_IO_Telnet_Exception("Failed to configure blocking on server ServerSocketChannel.", e);
		}

		try {
			ssc.bind(new InetSocketAddress(address, port));
		} catch (IOException e) {
			try {
				sel.close();
			} catch (IOException e2) {
				throw new Client_IO_Telnet_Exception(
						"ClientIOTelnet: Failed to close Selector while failed to bind address and socket to server ServerSocketChannel.",
						e2);
			}
			try {
				ssc.close();
			} catch (IOException e3) {
				throw new Client_IO_Telnet_Exception(
						"ClientIOTelnet: Failed to close server ServerSocketChannel while failed to bind address and socket to server ServerSocketChannel.",
						e3);
			}
			throw new Client_IO_Telnet_Exception(
					"ClientIOTelnet: Failed to bind address and socket to server ServerSocketChannel.", e);
		}

		try {
			ssc.register(sel, SelectionKey.OP_ACCEPT, null);
		} catch (ClosedChannelException e) {
			try {
				sel.close();
			} catch (IOException e2) {
				throw new Client_IO_Telnet_Exception(
						"ClientIOTelnet: Failed to close Selector while failed to register server ServerSocketChannel.",
						e2);
			}
			try {
				ssc.close();
			} catch (IOException e3) {
				throw new Client_IO_Telnet_Exception(
						"ClientIOTelnet: Failed to close server ServerSocketChannel while failed to register server ServerSocketChannel.",
						e3);
			}
			throw new Client_IO_Telnet_Exception("ClientIOTelnet: Failed to register server ServerSocketChannel.", e);
		}

		return sel;
	}

	/**
	 * Loops (waits and is woken up) checking for new input and output to be
	 * received and sent, and registers and {@link SelectionKey} changes.
	 */
	@Override
	public void run() {
		MyLogger.log(Level.INFO, "ClientIOTelnet: Starting ClientIOTelnet.");
		int numSelected = 0;

		running = true;
		while (running) {
			// MyLogger.log(Level.INFO, "Running ClientIOTelnet loop.");

			try {
				numSelected = sel.select();
			} catch (IOException e) {
				try {
					sel.close();
				} catch (IOException e2) {
					MyLogger.log(Level.SEVERE,
							"ClientIOTelnet: Failed to close Selector while failed to select on Selector.", e2);
					stopRunning();
					break;
				}
				try {
					ssc.close();
				} catch (IOException e3) {
					MyLogger.log(Level.SEVERE,
							"ClientIOTelnet: Failed to close server ServerSocketChannel while failed to select on Selector.",
							e3);
					stopRunning();
					break;
				}
				MyLogger.log(Level.SEVERE, "ClientIOTelnet: Failed to select on Selector.", e);
				stopRunning();
				break;
			}

			// If selector was woken up when running is not false, stops trying
			// to look for non existent selected keys.
			if (!running) {
				break;
			}

			// System.out.println("SELECTED: " + numSelected);
			Set<SelectionKey> selKeys = sel.selectedKeys();
			Iterator<SelectionKey> selIter = selKeys.iterator();

			while (selIter.hasNext()) {
				// Get next key and remove it from iterator.
				SelectionKey currKey = selIter.next();
				selIter.remove();

				if (!currKey.isValid()) {
					currKey.cancel();
					continue;
				}

				// Selected key is acceptable; a new client connection.
				if (currKey.isAcceptable()) {
					this.accept(currKey);
				}

				else if (currKey.isReadable()) {
					this.read(currKey);
				} else if (currKey.isWritable()) {
					this.write(currKey);
				}

			}

			synchronized (keyList) {
				// If selected 0, means was woken up after register but register
				// had not had time to take effect yet. Pause before re-select
				// to re-try.
				if (numSelected == 0) {
					if (keyList.isEmpty()) {
						MyLogger.log(Level.FINE, "ClientIOTelnet: Selected 0.");
						// MyLogger.log(Level.WARNING, "ClientIOTelnet:
						// Possible failure to register properly.");
						// int i = 0;
						// while (keyList.isEmpty() && (i <= 10)) {
						// i++;
						// try {
						// Thread.sleep(10);
						// } catch (InterruptedException e) {
						// }
						// }
					}
				}

				// if (!keyList.isEmpty()) {
				// System.out.println("KEYS DETECTED");
				// }

				// Do key registration actions queued in keyList
				while (!keyList.isEmpty()) {
					SelectItem item = keyList.remove(0);

					if (!item.getCcon().getSc().isConnected()) {
						try {
							item.getCcon().getSc().close();
						} catch (IOException e) {
							MyLogger.log(Level.SEVERE,
									"ClientIOTelnet: Failed to close disconnected SocketChannel on closed keyList item.",
									e);
						}
						continue;
					}

					if (item.key == SelectionKey.OP_READ) {
						// System.out.println("Registering for READ " +
						// Thread.currentThread().getName());
					} else if (item.key == SelectionKey.OP_WRITE) {
						// System.out.println("Registering for WRITE " +
						// Thread.currentThread().getName());
					} else if (item.key == 0) {
						// System.out.println("Registering for NONE " +
						// Thread.currentThread().getName());
					}
					SelectionKey foundKey = item.getCcon().getSc().keyFor(this.sel);
					if (foundKey == null) {
						try {
							item.getCcon().getSc().register(this.sel, item.key, item.getCcon());
						} catch (ClosedChannelException e) {
							MyLogger.log(Level.INFO, "ClientIOTelnet: Tried to register Selector on closed channel.",
									e);
						}
					} else {
						foundKey.interestOps(item.key);
					}
				}
			}
		}

		MyLogger.log(Level.INFO, "ClientIOTelnet: Initiating Telnet_IO shutdown.");
		clearResources();
		MyLogger.log(Level.INFO, "ClientIOTelnet: Gracefully closed Telnet_IO.");
	}

	private void accept(SelectionKey key) {
		if (!accepting) {
			MyLogger.log(Level.WARNING, "ClientIOTelnet: Refused accept on new connection.");
			return;
		}

		SocketChannel sc;
		try {
			// New client channel.
			sc = ((ServerSocketChannel) key.channel()).accept();
		} catch (IOException e) {
			MyLogger.log(Level.WARNING, "ClientIOTelnet: Failed to accept new client SocketChannel.", e);
			return;
		}

		try {
			// Sets new client channel into non blocking mode, as
			// NIO requires.
			sc.configureBlocking(false);
		} catch (IOException e) {
			MyLogger.log(Level.WARNING, "ClientIOTelnet: Failed to configure blocking on client SocketChannel.", e);
			try {
				sc.close();
			} catch (IOException e2) {
				MyLogger.log(Level.WARNING,
						"ClientIOTelnet: Failed to close client SocketChannel while failed to configure blocking on client SocketChannel.",
						e2);
			}
			return;
		}
		new Session(new Client_Connection_Telnet(this, sc));
	}

	private void read(SelectionKey key) {
		// System.out.println("CALL TO READ");

		// Number of reads, returned by the read operation.
		int numRead = 0;
		while (true) {
			// Clear buffer so its ready for new data.
			this.readBuffer.clear();
			try {
				numRead = ((SocketChannel) key.channel()).read(this.readBuffer);
				readBuffer.flip();
			} catch (IOException e) {
				// Client connection was shutdown remotely, abruptly.
				MyLogger.log(Level.WARNING, "ClientIOTelnet: Failed to read from SocketChannel to ByteBuffer.", e);
				key.cancel();
				((Client_Connection_Telnet) key.attachment()).close();
				return;
			}

			if (numRead == 0) {
				// Finished reading from channel.
				break;
			}

			if (numRead == -1) {
				// Client connection was shutdown remotely, cleanly.
				key.cancel();
				((Client_Connection_Telnet) key.attachment()).close();
				return;
			}

			// System.out.println("numRead: " + numRead);
			while (readBuffer.hasRemaining()) {
				byte b = readBuffer.get();

				((Client_Connection_Telnet) key.attachment()).readToConnectionPart((char) b);
			}
		}
	}

	private void write(SelectionKey currKey) {
		Client_Connection_Telnet ccon = (Client_Connection_Telnet) currKey.attachment();

		ByteBuffer buff;
		synchronized (ccon) {
			while ((buff = ccon.writeFromConnection()) != null) {
				try {
					((SocketChannel) currKey.channel()).write(buff);
				} catch (IOException e) {
					ccon.finishedWrite();
					MyLogger.log(Level.WARNING, "ClientIOTelnet: Failed to write to SocketChannel.", e);
				}
				// SocketChannel's internal buffer is full
				if (buff.remaining() > 0) {
					break;
				}
				ccon.finishedWrite();
			}

		}
	}

	/**
	 * Queue up changed to a SelectionKey for given connection.
	 * 
	 * @param ccon
	 * @param key
	 * @param wakeUp
	 *            Whether to wake up the selector or not (do not want to wake up if
	 *            queueing from selector's thread)
	 */
	public void addKeyQueue(Client_Connection_Telnet ccon, int key, boolean wakeUp) {
		synchronized (keyList) {
			for (SelectItem selItem : keyList) {
				if (ccon == selItem.getCcon()) {
					// System.out.println("FOUND SAME CCON!!!!!!!!");
					if (selItem.getKey() == SelectionKey.OP_READ) {
						if (key == SelectionKey.OP_READ) {
							MyLogger.log(Level.FINE, "Ignoring queue for READ when already queue for READ.");
							return;
						} else if (key == SelectionKey.OP_WRITE) {
							MyLogger.log(Level.FINE, "Allowing queue for WRITE when already queue for READ.");
							break;
						}
					} else if (selItem.getKey() == SelectionKey.OP_WRITE) {
						if (key == SelectionKey.OP_READ) {
							MyLogger.log(Level.FINE, "Ignoring queue for READ when already queue for WRITE.");
							return;
						} else if (key == SelectionKey.OP_WRITE) {
							MyLogger.log(Level.FINE, "Ignoring queue for WRITE when already queue for WRITE.");
							return;
						}
					}
					break;
				}
			}

			if (key == SelectionKey.OP_READ) {
				// System.out.println(
				// "Queueing key for READ " + Thread.currentThread().getName() +
				// ", wakeUp is: " + wakeUp);
			} else if (key == SelectionKey.OP_WRITE) {
				// System.out.println(
				// "Queueing key for WRITE " + Thread.currentThread().getName()
				// + ", wakeUp is: " + wakeUp);
			}
			keyList.add(new SelectItem(ccon, key));
		}
		if (wakeUp)

		{
			sel.wakeup();
		}
	}

	/**
	 * Tells thread to stop looping on next iteration, and wakes up
	 * {@link Selector}. Initiates the shutdown of the telnet IO thread.
	 */
	public void stopRunning() {
		this.running = false;
		sel.wakeup();
	}

	/**
	 * Cleans up telnet IO resources in case of shut down or telnet IO restart.
	 * Closes off all channels/sockets and removes {@link SelectionKey}s.
	 */
	public void clearResources() {
		if (this.ssc != null && this.ssc.isOpen()) {
			try {
				ssc.close();
				MyLogger.log(Level.INFO, "ClientIOTelnet: Shutdown ServerSocketChannel.");
			} catch (IOException e) {
				MyLogger.log(Level.WARNING, "ClientIOTelnet: IOException while closing ServerSocketChannel.", e);
			}
		}

		// Should do nothing if Sessions cleanly close, but here in case stray
		// Client_Connection_Telnet are left over.
		Iterator<SelectionKey> keys = this.sel.keys().iterator();
		while (keys.hasNext()) {
			SelectionKey key = keys.next();

			if (key.channel() instanceof SocketChannel) {
				Client_Connection_Telnet ccon = ((Client_Connection_Telnet) key.attachment());
				if (ccon != null) {
					ccon.close();
				}
			}

			key.cancel();
		}

		try {
			sel.close();
		} catch (IOException e) {
			MyLogger.log(Level.WARNING, "ClientIOTelnet: IOException while closing Selector.", e);
		}
	}

}
