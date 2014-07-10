package com.teltech.teaspoon;

public abstract class TeaspoonHandler {

	/**
	 * Called when connected to the server
	 */
	abstract public void onConnect();
	
	/**
	 * Called when the socket fails to connect
	 * 
	 * @param e Exception
	 */
	abstract public void onConnectionError(Exception e);
	
	/***
	 * Called when disconnected from the server
	 */
	abstract public void onDisconnect();
	
	/**
	 * Called when the socket receives a request
	 * 
	 * @param request
	 */
	abstract public void onReceivedRequest(Request request);
	
}
