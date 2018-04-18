package RoutingProtocol;

public class PingThread implements Runnable {

	private final RoutingProtocol routing;
	
	public PingThread(RoutingProtocol p) {
		routing = p;
	}

	//Ping every second
	@Override
	public void run() {
		while (true) {
			routing.pingTick();
			routing.sendPing();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}