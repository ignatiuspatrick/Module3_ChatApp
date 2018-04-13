package RoutingProtocol;

public class PingThread implements Runnable {

	private final RoutingProtocol routing;

	public PingThread(RoutingProtocol p) {
		routing = p;
	}

	@Override
	public void run() {
		while (true) {
			routing.pingTick();
			routing.sendPing();
			try {
				Thread.sleep(00);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
