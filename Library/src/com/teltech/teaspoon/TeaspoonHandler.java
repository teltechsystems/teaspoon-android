package com.teltech.teaspoon;

public abstract class TeaspoonHandler {

	/**
	 * Called when connected to the server
	 */
	abstract public void onConnect();
	
	/***
	 * Called when disconnected from the server
	 */
	abstract public void onDisconnect();
	
}
