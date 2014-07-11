package com.teltech.teaspoon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.util.Log;

public class Frame {

	public int opcode;
	public int priority;
	public int unused;
	public int method;
	public long resource;
	public long sequence;
	public long totalSequences;
	public byte[] requestIdentifier;
	public long payloadLength;
	public byte[] payload;
	
	/**
	 * Convert the frame to bytes so it can be sent over the socket
	 * 
	 * @return
	 */
	public byte[] data() {
		
		ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
		try {
			
			// Opcode + Priority
			dataStream.write((byte) (this.opcode <<4 | this.priority));
			
			// Unused + Method 
			dataStream.write((byte) (this.unused << 4 | this.method));
			
			// Resource
			dataStream.write((byte) (this.resource >> 8 & 0xFF));
			dataStream.write((byte) (this.resource & 0xFF));
			
			// Sequence
			dataStream.write((byte) (this.sequence >> 8 & 0xFF));
			dataStream.write((byte) (this.sequence & 0xFF));
			
			// Total Sequences
			dataStream.write((byte) (this.totalSequences >> 8 & 0xFF));
			dataStream.write((byte) (this.totalSequences & 0xFF));
			
			// Request Identifier
			dataStream.write(this.requestIdentifier);
			
			// Payload Length + Payload
			int payloadLength = 0;
			if (this.payload == null) {
				dataStream.write((byte) (payloadLength >> 24 & 0xFF));
				dataStream.write((byte) (payloadLength >> 16 & 0xFF));
				dataStream.write((byte) (payloadLength >> 8 & 0xFF));
				dataStream.write((byte) (payloadLength & 0xFF));
			} else {
				payloadLength = this.payload.length;
				dataStream.write((byte) (payloadLength >> 24 & 0xFF));
				dataStream.write((byte) (payloadLength >> 16 & 0xFF));
				dataStream.write((byte) (payloadLength >> 8 & 0xFF));
				dataStream.write((byte) (payloadLength & 0xFF));
				dataStream.write(this.payload);
			}
		} catch (IOException e) {
			Log.v("DEBUG", "Failed to convert frame to a Byte Array");
		}

		return dataStream.toByteArray();
	}
	
	/**
	 * Reset the frame
	 */
	public void reset() {
		opcode = 0;
		priority = 0;
		unused = 0;
		method = 0;
		resource = 0;
		sequence = 0;
		totalSequences = 0;
		requestIdentifier = null;
		payloadLength = 0;
		payload = null;
	}
	
	/**
	 * Return the Request Identifer as a string
	 * 
	 * @return String
	 */
	public String requestIdentifierString() {
		String identifier = "";
		for (int x = 0; x < 16; x++) {
			identifier += this.requestIdentifier[x];
		}
		return identifier;
	}
}
