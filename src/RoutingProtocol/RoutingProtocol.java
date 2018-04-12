package RoutingProtocol;



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class RoutingProtocol implements Runnable {
	
	private final MulticastSocket socket;
	private final InetAddress group;
	private byte seq = 1;
	private byte id;
	private byte ack;
	
	String ip = "224.0.1.46";
	int port = 2301;
	
	private Map<Byte, Byte[]> users = new HashMap<>();
	
	public static void main(String[] args) {
		try {
			RoutingProtocol o = new RoutingProtocol((byte) 7);
			o.scan();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public RoutingProtocol(byte i) throws IOException {
		id = i;
		group = InetAddress.getByName(ip);
		socket =  new MulticastSocket(port);
		socket.joinGroup(group);
		Thread n = new Thread(this);
		n.start();
	}
	
	public class Ping implements Runnable {
		
		private MulticastSocket ms;
		private byte compid;
		
		public Ping() {
			 try {
				ms = new MulticastSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			
			
		}
	}
	
	public void scan() {
		while (true) {
			Scanner scan = new Scanner(System.in);
			byte[] s = scan.nextLine().getBytes();
			outMessage(s);
		}
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
			byte[] recb = rec.getData();
			if (users.containsKey(recb[0])) {
				if ((users.get(recb[0])[0] + 1) % 200 == recb[1] % 200) {
					receiveMessage(recb);
				}
				relayMessage(recb);
			} else {
				receiveMessage(recb);
				users.put(recb[0], new Byte[] {recb[1], recb[2]});
				System.out.println("message relayed");
				try {
					socket.send(rec);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void receiveMessage(byte[] recb) {
		System.out.println("message received");
		sendAck();
	}
	
	private void sendAck() {
		byte[] b = new byte[] {this.id};
		DatagramPacket snd = new DatagramPacket(b, b.length, group, port);
		try {
			socket.send(snd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void relayMessage(byte[] message) {
		Byte[] bts = users.get(message[0]);
		if ((bts[0].byteValue() + 1) % 200 == message[1] % 200 || bts[0].byteValue() % 200 == message[1] % 200 && bts[1].byteValue() < message[2]) {
			bts[0] = message[1];
			bts[1] = message[2];
			DatagramPacket snd = new DatagramPacket(message, message.length, group, port);
			System.out.println("message relayed");
			try {
				socket.send(snd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void outMessage(byte[] message) {
		//seq += 1;
		// 0 to implement
		byte[] b = new byte[] {id, seq, 0, ack};
		byte[] out = new byte[message.length + b.length];
		System.arraycopy(b, 0, out, 0, b.length);
		System.arraycopy(message, 0, out, b.length, message.length);
		DatagramPacket snd = new DatagramPacket(out, out.length, group, port);
		try {
			socket.send(snd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}