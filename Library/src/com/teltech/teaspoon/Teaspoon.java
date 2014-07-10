package com.teltech.teaspoon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;

public class Teaspoon {

	private Socket socket;
	private enum FrameField {OPCODE, PRIORITY, UNUSED, METHOD, RESOURCE, SEQUENCE, TOTAL_SEQUENCES, REQUEST_IDENTIFIER, PAYLOAD_LENGTH, PAYLOAD}
	private FrameField currentFrameField;
	private ByteArrayOutputStream inputBuffer;
	protected boolean isConnecting;
	protected OutputStream outputStream;
	protected InputStream inputStream;
	protected String address;
	protected int port;
	protected TeaspoonHandler handler;
	
	/**
	 * Initialize the Teaspoon library
	 * 
	 * @param context	Application context
	 * @param address	Address of the server to connect to
	 * @param port		Port on the server to connect to
	 */
	public Teaspoon (Context context, String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	/**
	 * Connect to the server
	 * 
	 * @param timeout Timeout in seconds
	 */
	public void connect(int timeout) {
		this.connectSocket(timeout);
	}
	
	/**
	 * Disconnect the socket
	 */
	public void disconnect() {
		try {
			if (this.socket != null) {
				this.socket.close();
			}
			if (inputBuffer != null) {
				inputBuffer.reset();
			}
		} catch (IOException e) {
		}
	}
	
	/**
	 * Set the handler to be notified of events
	 * 
	 * @param handler Handler to receive events
	 */
	public void setHandler(TeaspoonHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * Connect the socket
	 */
	private void connectSocket(final int timeout) {
		if (this.isConnecting) {
			return;
		} else {
			this.isConnecting = true;
		}
		
		this.currentFrameField = FrameField.OPCODE;
		if (inputBuffer != null) {
			inputBuffer.reset();
		}
		
		final Teaspoon self = this;
		new Thread() {
			@Override
			public void run() {
				self.socket = new Socket();
				SocketAddress socketAddress = new InetSocketAddress(self.address, self.port);
				try {
					self.socket.connect(socketAddress, timeout * 1000);
					self.outputStream = self.socket.getOutputStream();
					self.isConnecting = false;
					if (self.handler != null) {
						self.handler.onConnect();
					}
					
					self.inputStream = socket.getInputStream();
					self.inputBuffer = new ByteArrayOutputStream(1024);
					byte[] buffer = new byte[1024];
					int bytesRead;
					 while ((bytesRead = self.inputStream.read(buffer)) != -1) {
		                 inputBuffer.write(buffer, 0, bytesRead);
		                 self.processInputBuffer();
		             }
				} catch (IOException e) {
					self.isConnecting = false;
					if ((self.handler != null) && (self.socket.isClosed() == false)) {
						self.handler.onConnectionError(e);
					}
				}
			}
		}.start();
	}
	
	private void processInputBuffer() {
		Log.v("DEBUG", "Process: " + inputBuffer.toByteArray().length + " = " + inputBuffer.toString());
		
		
		byte[] buffer = this.inputBuffer.toByteArray();
		
		switch (this.currentFrameField) {
			case OPCODE:
			case PRIORITY:
				
				if (buffer.length >= 1) {
					byte[] bytes = Arrays.copyOfRange(buffer, 0, 1);
					int opcode = bytes[0] >> 4;
					int priority = 0x0F & bytes[0];
					
					Log.v("DEBUG", "Opcode+Priority = " + opcode + ", " + priority);
				}
				break;
				
			case UNUSED:
				break;
				
			case METHOD:
				break;
				
			case RESOURCE:
				break;
				
			case SEQUENCE:
				break;
				
			case TOTAL_SEQUENCES:
				break;
				
			case REQUEST_IDENTIFIER:
				break;
				
			case PAYLOAD_LENGTH:
				break;
				
			case PAYLOAD:
				break;
			
		}
	}
}
