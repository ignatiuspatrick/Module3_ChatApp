package GUI;

import RoutingProtocol.FileTransferProtocol;

public class Startup {

	public static void main(String[] args) {
		new Startup();
	}
	
	private final UserInterface i;
	private FileTransferProtocol file;
	
	public Startup() {
		i = new UserInterface(this);
	}
	
	public void Connect(byte id, String password, String name) {
		file = new FileTransferProtocol(id, password, name);
		file.addObserver(i);
	}
	
	public void Disconnect() {
		file.getrouting().close();
	}

	
	public void Send(String s) {
		if (file != null) {
			file.sendMessage(s);
		}
	}
	
}
