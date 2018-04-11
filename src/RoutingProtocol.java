

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RoutingProtocol implements Runnable {
	
	private final MulticastSocket socket;
	private final InetAddress group;
	private byte seq = 1;
	private byte id;
	
	String ip = "224.0.1.46";
	int port = 2301;
	
	private Map<Byte, Byte[]> users = new HashMap<>();
	
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
			byte[] recb = rec.getData();
			if (users.containsKey(recb[0])) {
				relayMessage(recb);
			} else {
				users.put(recb[0], new Byte[] {recb[1], recb[2]});
				try {
					socket.send(rec);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void relayMessage(byte[] message) {
		Byte[] bts = users.get(message[0]);
		if ((bts[0].byteValue() + 1) % 200 == message[1] % 200 || bts[0].byteValue() % 200 == message[1] % 200 && bts[1].byteValue() < message[2]) {
			bts[0] = message[1];
			bts[1] = message[2];
			DatagramPacket snd = new DatagramPacket(message, message.length);
			try {
				socket.send(snd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void OutMessage(byte[] message) {
		byte[] b = new byte[] {id, seq, 0};
		byte[] out = new byte[message.length + b.length];
		System.arraycopy(b, 0, out, 0, b.length);
		System.arraycopy(message, 0, out, b.length, message.length);
		DatagramPacket snd = new DatagramPacket(out, out.length);
		try {
			socket.send(snd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}