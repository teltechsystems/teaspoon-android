package com.teltech.teaspoon;

import java.util.ArrayList;

import android.util.Log;

public class PriorityQueue {

	ArrayList<Request> queue = new ArrayList<Request>();
	
	public PriorityQueue() {
		
	}
	
	/**
	 * Add a request to the queue
	 * 
	 * @param request
	 */
	public void addRequest(Request request) {
		this.queue.add(request);
	}
	
	/**
	 * Returns the highest priority request in the queue
	 * 
	 * @return Request
	 */
	public Request highestRequest() {
		if (this.queue.isEmpty()) {
			return null;
		} else {
			return this.queue.get(0);
		}
	}
	
	/**
	 * Remove all requests from the queue
	 */
	public void removeAllRequests() {
		this.queue.clear();
	}
	
	/**
	 * Remove a request from the queue
	 */
	public void removeRequest(Request request) {
		this.queue.remove(request);
	}
}
