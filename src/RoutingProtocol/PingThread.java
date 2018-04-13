package RoutingProtocol;

public class PingThread implements Runnable {

	private final RoutingProtocol routing;

	public PingThread(RoutingProtocol p) {
		routing = p;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			routing.pingTick();
			routing.sendPing();
		}
	}

}
