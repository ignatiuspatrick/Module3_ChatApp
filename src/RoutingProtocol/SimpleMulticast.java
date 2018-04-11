package RoutingProtocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SimpleMulticast {
	
	private String ip = "230.0.0.0";
	private int port = 2424;
	
	public SimpleMulticast() {
		InetAddress group = null;
		try {
			group = InetAddress.getByName(ip);
			MulticastSocket s = new MulticastSocket(port);
			s.joinGroup(group);
			String sn = "hi";
			DatagramPacket n = new DatagramPacket(sn.getBytes(), sn.length(), group, port);
			s.send(n);
			byte[] buffer = new byte[1000];
			DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
			s.receive(recv);
			System.out.println(recv.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		SimpleMulticast sim = new SimpleMulticast();
		
	}

}