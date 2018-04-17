package Tests;

import java.util.Observable;
import java.util.Observer;

import RoutingProtocol.Message;

public class MessageHolder implements Observer {

	private Message m;
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof Message) {
			m = (Message) arg1;
		}
	}
	
	public Message getMessage() {
		return m;
	}

}
