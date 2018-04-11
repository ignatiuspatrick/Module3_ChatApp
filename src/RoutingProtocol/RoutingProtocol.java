package RoutingProtocol;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RoutingProtocol implements Runnable {
	
	private final MulticastSocket socket;
	private final InetAddress group;
	private byte seq = 1;
	private byte id;
	
	String ip = "224.0.1.46";
	int port = 2301;
	
	public static void main(String[] args) {
		try {
			RoutingProtocol o = new RoutingProtocol();
			o.OutMessage(new byte[] {2,1});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public RoutingProtocol() throws IOException {
		group = InetAddress.getByName(ip);
		socket =  new MulticastSocket(port);
		socket.joinGroup(group);
		Thread n = new Thread(this);
		n.start();
	}
	
	public void run() {
		while (true) {
			byte[] buf = new byte[1000];
			DatagramPacket rec = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(rec);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(new String(buf, 0, buf.length));
		}
	}
	
	
	
	public void OutMessage(byte[] message) {
		System.out.println(id);
		
	}
	
	public void assignid(byte d) {
		id = d;
	}
	
}