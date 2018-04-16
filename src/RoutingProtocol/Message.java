package RoutingProtocol;

public class Message {

	private final String name;
	private final long time;
	private final String message;
	
	public Message(String n, long t, String m) {
		name = n;
		time = t;
		message = m;
	}
	
	public String toString() {
		return name + ": " + message;
	}
	
	public long getTime() {
		return time;
	}
	
}
