package com.teltech.teaspoon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;

public class Teaspoon {

	private Socket socket;
	private enum FrameField {OPCODE, PRIORITY, UNUSED, METHOD, RESOURCE, SEQUENCE, TOTAL_SEQUENCES, REQUEST_IDENTIFIER, PAYLOAD_LENGTH, PAYLOAD}
	private FrameField currentFrameField;
	private byte[] inputBuffer;
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
	 * Concatenate two byte arrays
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	byte[] concatenateByteArrays(byte[] a, byte[] b) {
	    byte[] result = new byte[a.length + b.length]; 
	    System.arraycopy(a, 0, result, 0, a.length); 
	    System.arraycopy(b, 0, result, a.length, b.length); 
	    return result;
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
		this.inputBuffer = new byte[0];
		
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
						 self.inputBuffer = buffer;
						 //self.concatenateByteArrays(inputBuffer, buffer);
						 //Log.v("DEBUG", "Concat: " + inputBuffer.length + " = " + buffer.length);
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
	
	 public int byteArrayToInt(byte[] b) {
		    if (b.length == 4)
		      return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
		          | (b[3] & 0xff);
		    else if (b.length == 2)
		      return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

		    return 0;
		  }
	 
	private void processInputBuffer() {
		//Log.v("DEBUG", "Process: " + this.inputBuffer.length);
		switch (this.currentFrameField) {
			case OPCODE:
			case PRIORITY:
				if (inputBuffer.length >= 1) {
					byte[] bytes = Arrays.copyOfRange(this.inputBuffer, 0, 1);
					int opcode = bytes[0] >> 4;
					int priority = 0x0F & bytes[0];
					
					Log.v("DEBUG", "Opcode+Priority = " + opcode + ", " + priority);
					
					this.inputBuffer = Arrays.copyOfRange(this.inputBuffer, 1, this.inputBuffer.length - 1);
					
					this.currentFrameField = FrameField.UNUSED;
					this.processInputBuffer();
				}
				break;
				
			case UNUSED:
			case METHOD:
				if (inputBuffer.length >= 1) {
					byte[] bytes = Arrays.copyOfRange(this.inputBuffer, 0, 1);
					int unused = bytes[0] >> 4;
					int method = 0x0F & bytes[0];
					
					Log.v("DEBUG", "Unused+Method = " + unused + ", " + method);
					
					this.inputBuffer = Arrays.copyOfRange(this.inputBuffer, 1, this.inputBuffer.length - 1);
					
					this.currentFrameField = FrameField.RESOURCE;
					this.processInputBuffer();
				}
				break;
				
			case RESOURCE:
				if (inputBuffer.length >= 2) {
					byte[] bytes = Arrays.copyOfRange(inputBuffer, 0, 2);
					
					int resource = this.byteArrayToInt(bytes);
					
					Log.v("DEBUG", "Resource = " + resource);
					
					this.inputBuffer = Arrays.copyOfRange(this.inputBuffer, 2, this.inputBuffer.length - 2);
					
					this.currentFrameField = FrameField.SEQUENCE;
					this.processInputBuffer();
				}
				break;
				
			case SEQUENCE:
				if (inputBuffer.length >= 2) {
					byte[] bytes = Arrays.copyOfRange(inputBuffer, 0, 2);
					
					int sequence = this.byteArrayToInt(bytes);
					
					Log.v("DEBUG", "Sequence = " + sequence);
					
					this.inputBuffer = Arrays.copyOfRange(this.inputBuffer, 2, this.inputBuffer.length - 2);
					
					this.currentFrameField = FrameField.TOTAL_SEQUENCES;
					this.processInputBuffer();
				}
				break;
				
			case TOTAL_SEQUENCES:
				if (inputBuffer.length >= 2) {
					byte[] bytes = Arrays.copyOfRange(inputBuffer, 0, 2);
					
					int totalSequences = this.byteArrayToInt(bytes);
					
					Log.v("DEBUG", "totalSequences = " + totalSequences);
					
					this.inputBuffer = Arrays.copyOfRange(this.inputBuffer, 2, this.inputBuffer.length - 2);
					
					this.currentFrameField = FrameField.REQUEST_IDENTIFIER;
					this.processInputBuffer();
				}
				break;
				
			case REQUEST_IDENTIFIER:
				if (inputBuffer.length >= 16) {
					byte[] bytes = Arrays.copyOfRange(inputBuffer, 0, 16);
					
					int requestIdentifier = this.byteArrayToInt(bytes);
					
					Log.v("DEBUG", "requestIdentifier = " + requestIdentifier);
					
					this.inputBuffer = Arrays.copyOfRange(this.inputBuffer, 16, this.inputBuffer.length - 16);
					
					this.currentFrameField = FrameField.PAYLOAD_LENGTH;
					this.processInputBuffer();
				}
				break;
				
			case PAYLOAD_LENGTH:
				if (inputBuffer.length >= 4) {
					byte[] bytes = Arrays.copyOfRange(inputBuffer, 0, 4);
					
					int payloadLength = this.byteArrayToInt(bytes);
					
					Log.v("DEBUG", "payloadLength = " + payloadLength);
					
					this.inputBuffer = Arrays.copyOfRange(this.inputBuffer, 4, this.inputBuffer.length - 4);
					
					this.currentFrameField = FrameField.PAYLOAD;
					this.processInputBuffer();
				}
				break;
				
			case PAYLOAD:
				break;
			
		}
	}
}
