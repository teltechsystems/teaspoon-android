package com.teltech.teaspoon;

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
}
