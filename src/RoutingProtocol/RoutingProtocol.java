package RoutingProtocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Security.SessionProtocol;

public class RoutingProtocol implements Runnable {

	private final MulticastSocket socket;
	private final InetAddress group;
	private byte seq = 0;
	private byte id;
	private ReentrantLock lock;
	private byte specseq = 0;
	private static int HEADER_LENGTH = 6;
	private SessionProtocol sess;

	String ip = "228.133.202.100";
	int port = 2301;

	private Map<Byte, Byte[]> users;
	private Map<Byte, Byte> pingmap;
	private Map<Byte, Boolean> ackmap;

	public static void main(String[] args) {
		try {
			RoutingProtocol o = new RoutingProtocol((byte) 2, "hellogroup5");
			o.scan();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RoutingProtocol(byte i, String password) throws IOException {
		users = new HashMap<>();
		pingmap = new HashMap<>();
		lock = new ReentrantLock();
		sess = new SessionProtocol(password);
		id = i;
		group = InetAddress.getByName(ip);
		socket = new MulticastSocket(port);
		socket.joinGroup(group);
		Thread n = new Thread(this);
		n.start();
		Thread ping = new Thread(new PingThread(this));
		ping.start();
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
			byte[] recb = new byte[] { -1 };
			byte[] buf = new byte[1000];
			DatagramPacket rec = new DatagramPacket(buf, buf.length);
			do {
				try {
					socket.receive(rec);
				} catch (IOException e) {
					e.printStackTrace();
				}
				recb = rec.getData();
				// System.out.println("Message Received from: " + recb[0]);
			} while (recb[0] == id);
			//
			// System.out.println("Message Received from: " + recb[0] + " " + recb[1] + " "
			// + recb[2] + " " + recb[3]);

			// Ping from other computers.
			if (recb[3] == -2) {
				if (!users.containsKey(recb[0]) || !pingmap.containsKey(recb[0])) {
					users.put(recb[0], new Byte[] { recb[1], recb[2], -1 });
					relayMessage(recb);
				}
				pingmap.put(recb[0], (byte) 5);
				Byte[] bts = users.get(recb[0]);
				if (bts != null) {
					if ((bts[2].byteValue()) != recb[2]) {
						relayMessage(recb);
					}
				}
			}

			// Normal messages.
			if (recb[3] == -1) {
				if (users.containsKey(recb[0])) {
					System.out.println(pingmap.containsKey(recb[0]) + " " + (users.get(recb[0])[0] + 1) % 100 + " "
							+ recb[1] % 100);
				}
				if ((pingmap.containsKey(recb[0]) && (users.get(recb[0])[0] + 1) % 100 == recb[1] % 100)) {
					receiveMessage(recb);
				}
				Byte[] bts = users.get(recb[0]);
				if (bts != null) {
					if ((bts[0].byteValue() + 1) % 100 == recb[1] % 100
							|| bts[0].byteValue() % 100 == recb[1] % 100 && bts[1].byteValue() < recb[2]) {
						relayMessage(recb);
					}
				}
			}

			// acks
			if (recb[3] >= 0) {
				if (users.containsKey(recb[0])) {
					if (users.get(recb[0])[2].byteValue() != recb[2]) {
						relayMessage(recb);
					}
				}

				if (ackmap != null) {
					if (recb[3] == seq && recb[4] == id && ackmap.containsKey(recb[0])) {
						ackmap.put(recb[0], true);
					}
				}
			}
		}
	}

	// When a new message is received.
	public void receiveMessage(byte[] recb) {
		System.out.println("message received!");
		recb = sess.decryptPlainText(recb, sess.getAuthKey()); // decrypt with authKey
		byte[] header = new byte[HEADER_LENGTH];
		System.arraycopy(recb, 0, header, 0, HEADER_LENGTH);
		int payloadlength = (int) header[5];
		byte[] sessionKey = new byte[recb.length - HEADER_LENGTH - payloadlength];
		System.arraycopy(recb, HEADER_LENGTH + payloadlength + 1, sessionKey, 0, HEADER_LENGTH - payloadlength);
		byte[] plaintext = new byte[payloadlength];
		System.arraycopy(recb, HEADER_LENGTH + 1, plaintext, 0, payloadlength);
		plaintext = sess.decryptPlainText(plaintext, new SecretKeySpec(sessionKey, "AES"));
		System.out.println(new String(plaintext));
	}

	public void relayMessage(byte[] message) {
		Byte[] bts = users.get(message[0]);
		bts[0] = message[1];
		if (message[3] != -1) {
			bts[2] = message[2];
		} else {
			bts[1] = message[2];
		}
		users.put(message[0], new Byte[] { bts[0], bts[1], bts[2] });
		DatagramPacket snd = new DatagramPacket(message, message.length, group, port);
		try {
			socket.send(snd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (message[3] == -1) {
			sendAck(message[1], message[0]);
		}
	}

	// Tick for the computers that we expect acks from.
	public void pingTick() {
		lock.lock();
		try {
			List<Byte> rlist = new ArrayList<>();
			for (Byte a : pingmap.keySet()) {
				Byte b = pingmap.get(a);
				b = (byte) (b.byteValue() - 1);
				pingmap.put(a, b);
				if (b == 0) {
					rlist.add(a);
					System.out.println("User disconnected");
				}
			}
			for (Byte host : rlist) {
				pingmap.remove(host);
			}

		} finally {
			lock.unlock();
		}
	}

	// to the next sequence number
	public byte nextSeq() {
		seq++;
		if (seq == 100) {
			seq = 0;
		}
		return seq;
	}

	// to the next sequence number
	public byte nextSpecSeq() {
		specseq++;
		if (specseq == 100) {
			specseq = 0;
		}
		return specseq;
	}

	// send acks for received messages.
	public void sendAck(byte ack, byte ackid) {
		lock.lock();
		try {
			byte[] b = new byte[] { id, seq, nextSpecSeq(), ack, ackid };
			DatagramPacket snd = new DatagramPacket(b, b.length, group, port);
			try {
				socket.send(snd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			lock.unlock();
		}
	}

	public void sendPing() {
		lock.lock();
		try {
			byte[] b = new byte[] { id, seq, nextSpecSeq(), -2, -2 };

			DatagramPacket snd = new DatagramPacket(b, b.length, group, port);
			try {
				socket.send(snd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			lock.unlock();
		}
	}

	// used by upper layer to send messages.
	public void outMessage(byte[] message) {
		lock.lock();
		try {
			SecretKey s = sess.generateSessionKey(); // generate session key
			message = sess.encryptPlainText(message, s); // encrypt plain message with session key
			byte[] b = new byte[] { id, nextSeq(), 0, -1, -1, (byte) message.length };
			byte[] out = new byte[message.length + b.length + s.getEncoded().length];
			System.arraycopy(b, 0, out, 0, b.length);
			System.arraycopy(message, 0, out, b.length, message.length);
			System.arraycopy(s.getEncoded(), 0, out, b.length + message.length, s.getEncoded().length);
			out = sess.encryptPlainText(out, sess.getAuthKey()); // encrypt
			DatagramPacket snd = new DatagramPacket(out, out.length, group, port);
			ackmap = new HashMap<>();
			for (Byte host : pingmap.keySet()) {
				ackmap.put(host, false);
			}
			byte re = 0;
			while (true) {
				try {
					socket.send(snd);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!ackmap.containsValue(false)) {
					System.out.println("Done");
					break;
				} else if (re > 20) {
					System.out.println("Timeout");
					for (Byte host : ackmap.keySet()) {
						if (ackmap.get(host) == false) {
							pingmap.remove(host);
						}
					}
					break;
				}
				System.out.println("resending");
				byte[] newsnd = snd.getData();
				re++;
				newsnd[2] = re;
				snd = new DatagramPacket(newsnd, newsnd.length, group, port);
			}

		} finally {
			lock.unlock();
		}
	}

}
