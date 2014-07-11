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
	
	/**
	 * Called when the request times out waiting for a response
	 */
	abstract public void onTimeout();
	
	/**
	 * Called when the request is aborted such as when the socket is disconnected or an error occurs
	 */
	abstract public void onAborted();
}
