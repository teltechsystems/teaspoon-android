package com.teltech.teaspoon;

public abstract class RequestHandler {

	/**
	 * Called when the server sends a response
	 * 
	 * @param method
	 * @param resource
	 * @param priority
	 * @param payload
	 */
	abstract public void onReceivedResponse(int method, long resource, int priority, byte[] payload);
	
}
