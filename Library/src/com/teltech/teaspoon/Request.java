package com.teltech.teaspoon;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import android.util.Log;

public class Request {

	public int MAX_PAYLOAD_BYTES = 12;
	
	public int opcode;
	public int priority;
	public int method;
	public long resource;
	public byte[] requestIdentifier;
	private byte[] payload;
	
	public int responseMethod;
	public long responseResource;
	public int responsePriority;
	private ByteArrayOutputStream responsePayload;
	private RequestHandler handler;
	
	private int currentSequence;
	private int totalSequences;
	
	/**
	 * Initialize the request
	 */
	public Request() {
		this.currentSequence = 0;
		this.totalSequences = 1;
		this.responsePayload = new ByteArrayOutputStream();
		this.opcode = 0x2;
		this.priority = 1;
	}
	
	/**
	 * Returns whether or not there are more frame to output
	 * 
	 * @return
	 */
	public boolean hasMoreFrames() {
		if ((this.currentSequence + 1) > this.totalSequences) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Returns the next frame to send over the socket
	 * 
	 * @return Frame or null
	 */
	public Frame nextOutputFrame () {
		
		if (this.hasMoreFrames() == false) {
			return null;
		}
		
		// Get the payload data for the sequence
		int dataStart = this.currentSequence * this.MAX_PAYLOAD_BYTES;
		int dataLength = 0;
		if (this.payload != null) {
			dataLength = this.payload.length - dataStart;
		}
		if (dataLength > this.MAX_PAYLOAD_BYTES) {
			dataLength = this.MAX_PAYLOAD_BYTES;
		}

		byte[] payload = (this.payload == null) ? null : Arrays.copyOfRange(this.payload, dataStart, dataStart + dataLength);
		
		// Generate the frame
		Frame frame = new Frame();
		frame.opcode = this.opcode;
		frame.priority = this.priority;
		frame.method = this.method;
		frame.resource = this.resource;
		frame.sequence = this.currentSequence;
		frame.totalSequences = this.totalSequences;
		frame.requestIdentifier = this.requestIdentifier;
		frame.payload = payload;
		
		this.currentSequence++;
		return frame;
	}
	
	/**
	 * Receive a frame from the socket and process it
	 * 
	 * @param frame
	 */
	public void receivedFrame(Frame frame) {
		this.requestIdentifier = frame.requestIdentifier;
		this.responseMethod = frame.method;
		this.responseResource = frame.resource;
		this.responsePriority = frame.priority;
		responsePayload.write(frame.payload, 0, frame.payload.length);
		if ((frame.sequence + 1) == frame.totalSequences) {
			if (this.handler != null) {
				this.handler.onReceivedResponse(this.responseMethod, this.responseResource, this.responsePriority, this.responsePayload.toByteArray());
			}
		}
	}
	
	/**
	 * Set the payload
	 */
	public void setPayload(byte[] payload) {
		this.currentSequence = 0;
		this.totalSequences = (int) Math.ceil((float)payload.length / (float)this.MAX_PAYLOAD_BYTES);
		this.payload = payload;
	}
	
	/**
	 * Set the handler to be notified of events
	 * 
	 * @param handler Handler to receive events
	 */
	public void setHandler(RequestHandler handler) {
		this.handler = handler;
	}
}
