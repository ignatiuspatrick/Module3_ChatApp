package RoutingProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;
import java.util.Set;

import GUI.Startup;

public class FileTransferProtocol extends Observable {
	
	private Map<Byte, String> usernames = new HashMap<>();
	private RoutingProtocol routing;
	
	public static byte TIME_LENGTH = 13;
	
	public FileTransferProtocol(byte i, String password, String name) {
		try {
			routing = new RoutingProtocol(this, i, password, name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Send a message to the RoutingProtocol
	public void sendMessage(String message) {
		Long timel = System.currentTimeMillis();
		byte[] time = timel.toString().getBytes();
		byte[] text = message.getBytes();
		byte[] out = new byte[time.length + text.length];
		System.arraycopy(time, 0, out, 0, TIME_LENGTH);
		System.arraycopy(text, 0, out, TIME_LENGTH, text.length);
		routing.outMessage(out);
		
	}
	
	// Receive message from RoutingProtocol and send to ui
	public void receiveMessage(byte id, byte[] message) {
		byte[] time = new byte[TIME_LENGTH];
		byte[] text = new byte[message.length - TIME_LENGTH];
		System.arraycopy(message, 0, time, 0, TIME_LENGTH);
		System.arraycopy(message, TIME_LENGTH, text, 0, message.length - TIME_LENGTH);
		String name = "unknown";
		if(usernames.containsKey(id)) {
			name = usernames.get(id);
		}
		setChanged();
		notifyObservers(new Message(name, new Long(new String(time)), new String(text)));
	}
	
	// Update map with id and username
	public void updateList(byte id, byte[] n) {
		usernames.put(id, new String(n));
	}
	
	// Get the RoutingProtocol
	public RoutingProtocol getrouting() {
		return routing;
	}

	// Send an update to the ui with the names of the currently connected nodes
	public void sendUpdate(Set<Byte> ping) {
		List<String> l = new ArrayList<>();
		for (Byte b: ping) {
			if (usernames.containsKey(b)) {
				l.add(usernames.get(b));
			}
		}
		setChanged();
		notifyObservers(l.toArray());
	}
	
}
