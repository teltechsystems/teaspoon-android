package com.teltech.teaspoon;

import java.io.ByteArrayOutputStream;

import android.util.Log;

public class Request {

	public int opcode;
	public int priority;
	public int method;
	public long resource;
	public byte[] requestIdentifier;
	public byte[] payload;
	
	public int responseMethod;
	public long responseResource;
	public int responsePriority;
	private ByteArrayOutputStream responsePayload;
	private RequestHandler handler;
	
	/**
	 * Initialize the request
	 */
	public Request() {
		this.responsePayload = new ByteArrayOutputStream();
		this.opcode = 0x2;
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
	 * Set the handler to be notified of events
	 * 
	 * @param handler Handler to receive events
	 */
	public void setHandler(RequestHandler handler) {
		this.handler = handler;
	}
}
