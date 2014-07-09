package com.teltech.teaspoon;

import android.content.Context;
import android.util.Log;

public class Teaspoon {

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
	 */
	public void connect() {
		this.handler.onConnect();
	}
	
	/**
	 * Set the handler to be notified of events
	 * 
	 * @param handler Handler to receive events
	 */
	public void setHandler(TeaspoonHandler handler) {
		this.handler = handler;
	}
}
