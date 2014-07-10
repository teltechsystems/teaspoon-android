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
	private Frame frame;
	private int OPCODE_PING = 0x9;
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
		this.frame = new Frame();
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
		this.inputBuffer = new ByteArrayOutputStream();
		
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
	
	/**
	 * Convert a byte array to a long
	 * 
	 * @param b Byte Array to convert
	 * @return long
	 */
	public long byteArrayToLong(byte[] b) {
	    if (b.length == 4)
	      return (b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
	          | (b[3] & 0xff)) & 0x00ffffffff;
	    else if (b.length == 2)
	      return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

	    return 0;
	}
	
	/**
	 * Reply to a PING
	 */
	public void PONG() {
		
	}
	
	/**
	 * Process the current frame
	 */
	private void processFrame() {
		if (frame.opcode == OPCODE_PING) {
			Log.v("DEBUG", ">>>>>>>>>>>>>>>>>>>> PING!");
		} else {
			Log.v("DEBUG", ">>>>>>>>>>>>>>>>>>>> Process Frame");
		}
	}
	 
	/**
	 * Process the input buffer 
	 */
	private void processInputBuffer() {
		
		byte[] bufferBytes = this.inputBuffer.toByteArray();
		
		switch (this.currentFrameField) {
			case OPCODE:
			case PRIORITY:
				if (bufferBytes.length >= 1) {
					byte[] bytes = Arrays.copyOfRange(bufferBytes, 0, 1);
					frame.opcode = (bytes[0] >> 4) & 0x0f;
					frame.priority = 0x0F & bytes[0];

					byte[] buffer = Arrays.copyOfRange(bufferBytes, 1, bufferBytes.length);
					this.inputBuffer.reset();
					this.inputBuffer.write(buffer, 0, buffer.length);
					
					this.currentFrameField = FrameField.UNUSED;
					this.processInputBuffer();
				}
				break;
				
			case UNUSED:
			case METHOD:
				if (bufferBytes.length >= 1) {
					byte[] bytes = Arrays.copyOfRange(bufferBytes, 0, 1);
					frame.unused = (bytes[0] >> 4) & 0x0f;
					frame.method = 0x0F & bytes[0];

					byte[] buffer = Arrays.copyOfRange(bufferBytes, 1, bufferBytes.length);
					this.inputBuffer.reset();
					this.inputBuffer.write(buffer, 0, buffer.length);
					
					this.currentFrameField = FrameField.RESOURCE;
					this.processInputBuffer();
				}
				break;
				
			case RESOURCE:
				if (bufferBytes.length >= 2) {
					byte[] bytes = Arrays.copyOfRange(bufferBytes, 0, 2);
					frame.resource = this.byteArrayToLong(bytes);
					
					byte[] buffer = Arrays.copyOfRange(bufferBytes, 2, bufferBytes.length);
					this.inputBuffer.reset();
					this.inputBuffer.write(buffer, 0, buffer.length);
					
					this.currentFrameField = FrameField.SEQUENCE;
					this.processInputBuffer();
				}
				break;
				
			case SEQUENCE:
				if (bufferBytes.length >= 2) {
					byte[] bytes = Arrays.copyOfRange(bufferBytes, 0, 2);
					frame.sequence = this.byteArrayToLong(bytes);
					
					byte[] buffer = Arrays.copyOfRange(bufferBytes, 2, bufferBytes.length);
					this.inputBuffer.reset();
					this.inputBuffer.write(buffer, 0, buffer.length);
					
					this.currentFrameField = FrameField.TOTAL_SEQUENCES;
					this.processInputBuffer();
				}
				break;
				
			case TOTAL_SEQUENCES:
				if (bufferBytes.length >= 2) {
					byte[] bytes = Arrays.copyOfRange(bufferBytes, 0, 2);					
					frame.totalSequences = this.byteArrayToLong(bytes);

					byte[] buffer = Arrays.copyOfRange(bufferBytes, 2, bufferBytes.length);
					this.inputBuffer.reset();
					this.inputBuffer.write(buffer, 0, buffer.length);
					
					this.currentFrameField = FrameField.REQUEST_IDENTIFIER;
					this.processInputBuffer();
				}
				break;
				
			case REQUEST_IDENTIFIER:
				if (bufferBytes.length >= 16) {
					frame.requestIdentifier = Arrays.copyOfRange(bufferBytes, 0, 16);

					byte[] buffer = Arrays.copyOfRange(bufferBytes, 16, bufferBytes.length);
					this.inputBuffer.reset();
					this.inputBuffer.write(buffer, 0, buffer.length);
					
					this.currentFrameField = FrameField.PAYLOAD_LENGTH;
					this.processInputBuffer();
				}
				break;
				
			case PAYLOAD_LENGTH:
				if (bufferBytes.length >= 4) {
					byte[] bytes = Arrays.copyOfRange(bufferBytes, 0, 4);					
					frame.payloadLength = this.byteArrayToLong(bytes);

					byte[] buffer = Arrays.copyOfRange(bufferBytes, 4, bufferBytes.length);
					this.inputBuffer.reset();
					this.inputBuffer.write(buffer, 0, buffer.length);
					
					this.currentFrameField = FrameField.PAYLOAD;
					this.processInputBuffer();
				}
				break;
				
			case PAYLOAD:
				if (frame.payloadLength == 0) {
					this.processFrame();
					this.currentFrameField = FrameField.OPCODE;
					this.processInputBuffer();
				} else {
					if (bufferBytes.length >= frame.payloadLength) {
						frame.payload = Arrays.copyOfRange(bufferBytes, 0, (int) frame.payloadLength);
						
						byte[] buffer = Arrays.copyOfRange(bufferBytes, (int) frame.payloadLength, bufferBytes.length);
						this.inputBuffer.reset();
						this.inputBuffer.write(buffer, 0, buffer.length);
						
						this.processFrame();
						this.currentFrameField = FrameField.OPCODE;
						this.processInputBuffer();
					}
				}
				break;
			
		}
	}
}
