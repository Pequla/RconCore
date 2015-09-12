package net.kronos.rkon.core;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import net.kronos.rkon.core.ex.AuthenticationException;

public class Rcon {
	
	private Socket socket;
	private int requestId;
	
	private final Object sync = new Object();

	/**
	 * Create an unconnected Rcon object
	 */
	public Rcon() {
		this.socket = new Socket();
		this.requestId = new Random().nextInt();
	}
	
	/**
	 * Create, connect and authenticate a new Rcon object
	 * 
	 * @param host Rcon server address
	 * @param port Rcon server port
	 * @param password Rcon server password
	 * 
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public Rcon(String host, int port, byte[] password) throws IOException, AuthenticationException {
		this();
		this.connect(host, port, password);
	}
	
	/**
	 * Connect to a rcon server
	 * 
	 * @param host Rcon server address
	 * @param port Rcon server port
	 * @param password Rcon server password
	 * 
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public void connect(String host, int port, byte[] password) throws IOException, AuthenticationException {
		if(host == null || host.trim().isEmpty()) {
			throw new IllegalArgumentException("Host can't be null or empty");
		}
		
		if(port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port is out of range");
		}
		
		// Connect to the rcon server
		synchronized(sync) {
			// We can't reuse a socket, so we need a new one
			this.socket = new Socket(host, port);
		}
		
		// Send the auth packet
		RconPacket res = this.send(RconPacket.SERVERDATA_AUTH, password);
		
		// Auth failed
		if(res.getRequestId() == -1) {
			throw new AuthenticationException("Password rejected by server");
		}
	}
	
	/**
	 * Disconnect from the current server
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		synchronized(sync) {
			this.socket.close();
		}
	}
	
	/**
	 * Send a command to the server
	 * 
	 * @param payload The command to send
	 * @return A RconPacket containing the response payload (use {@link RconPacket#getPayload()})
	 * 
	 * @throws IOException
	 */
	public RconPacket command(String payload) throws IOException {
		if(payload == null || payload.trim().isEmpty()) {
			throw new IllegalArgumentException("Payload can't be null or empty");
		}
		
		return this.send(RconPacket.SERVERDATA_EXECCOMMAND, payload.getBytes());
	}
	
	private RconPacket send(int type, byte[] payload) throws IOException {
		synchronized(sync) {
			return RconPacket.send(this, type, payload);
		}
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public int getRequestId() {
		return requestId;
	}

}