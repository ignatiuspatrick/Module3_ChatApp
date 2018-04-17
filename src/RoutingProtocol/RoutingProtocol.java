package RoutingProtocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Security.SessionProtocol;

public class RoutingProtocol implements Runnable {

	private MulticastSocket socket;
	private final InetAddress group;
	private byte seq = 0;
	private byte id;
	private ReentrantLock lock;
	private byte specseq = 0;
	private static int HEADER_LENGTH = 6;
	private SessionProtocol sess;

	String ip = "228.133.202.101";
	int port = 2301;
	
	private final byte[] password;
	private final byte[] name;
	private final FileTransferProtocol file;
	private Map<Byte, Byte[]> users;
	private Map<Byte, Byte> pingmap;
	private Map<Byte, Boolean> ackmap;

	public RoutingProtocol(FileTransferProtocol f, byte i, String pass, String n) throws IOException {
		file = f;
		name = n.getBytes();
		users = new HashMap<>();
		pingmap = new HashMap<>();
		lock = new ReentrantLock();
		sess = new SessionProtocol(pass);
		password = pass.getBytes();
		id = i;
		group = InetAddress.getByName(ip);
		socket = new MulticastSocket(port);
		socket.joinGroup(group);
		Thread t = new Thread(this);
		t.start();
		Thread ping = new Thread(new PingThread(this));
		ping.start();
	}



	public void run() {
		while (true) {
			byte[] buf = new byte[1000];
			byte[] recb = new byte[5];
			byte[] drev = new byte[5];
			boolean flag;
			DatagramPacket rec = new DatagramPacket(buf, buf.length);
			do {
				flag = false;
				try {
					socket.receive(rec);
				} catch (IOException e) {
					try {
						socket = new MulticastSocket(port);
					} catch (IOException e1) {
						System.out.println("Failed to create socket");
						flag = true;
						continue;
					}
				}
				byte[] temp = rec.getData();
				drev = new byte[temp[0]];
				System.arraycopy(temp, 1, drev, 0, temp[0]);;
				//System.out.println(new String(recb));
				//System.out.println("Receiver length: " + recb.length);
				//System.out.println("message received!");
				try {
					drev = sess.decryptPlainText(drev, sess.getAuthKey());
					
				} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
					System.out.println("Failed to decrypt messages");
					flag = true;
					continue;
				} // decrypt with authKey
				
				for (int c = 0; c < password.length; c++) {
					try {
						if (drev[c] != password[c]) {
							System.out.println("Wrong Password");
							flag = true;
							break;
						}
					} catch(ArrayIndexOutOfBoundsException e) {
						System.out.println("Password Error");
						flag = true;
						break;
					}
				}
				if (flag == true) {
					continue;
				}
				recb = new byte[temp[0] - password.length];
				System.arraycopy(drev, password.length, recb, 0, drev.length - password.length);
				//System.out.println(recb[0]);
			} while (recb[0] == id || flag == true);
			// Ping from other computers.
			if (recb[3] == -2) {
				if (!users.containsKey(recb[0]) || !pingmap.containsKey(recb[0])) {
					users.put(recb[0], new Byte[] { recb[1], recb[2], -1 });
					relayMessage(recb);
				}
				pingmap.put(recb[0], (byte) 5);
				byte[] n = new byte[recb.length - HEADER_LENGTH];
				System.arraycopy(recb, HEADER_LENGTH, n, 0, recb.length - HEADER_LENGTH);
				file.updateList(recb[0], n);
				Byte[] bts = users.get(recb[0]);
				if (bts != null) {
					if ((bts[2].byteValue()) != recb[2]) {
						relayMessage(recb);
					}
				}
			}

			// Normal messages.
			if (recb[3] == -1) {
				//if (users.containsKey(recb[0])) {
				//	System.out.println(pingmap.containsKey(recb[0]) + " " + (users.get(recb[0])[0] + 1) % 100 + " "
				//			+ recb[1] % 100);
				//}
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
		try {
		int payloadlength = (int) recb[5];
		byte[] sessionKey = new byte[16];
		System.arraycopy(recb, HEADER_LENGTH + payloadlength, sessionKey, 0, 16);
		byte[] plaintext = new byte[payloadlength];
		System.arraycopy(recb, HEADER_LENGTH, plaintext, 0, payloadlength);
		plaintext = sess.decryptPlainText(plaintext, new SecretKeySpec(sessionKey, "AES"));
		file.receiveMessage(recb[0], plaintext);
		System.out.println(new String(plaintext));
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Failed to decrypt incoming payload");
			e.printStackTrace();
		}
		/*
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
		
		*/
	}

	public void relayMessage(byte[] message) {
		Byte[] bts = users.get(message[0]);
		bts[0] = message[1];
		if (message[3] != -1) {
			bts[2] = message[2];
		} else {
			bts[1] = message[2];
		}
		users.put(message[0], new Byte[] {bts[0], bts[1], bts[2]});
		Send(message);
		if (message[3] == -1) {
			sendAck(message[1], message[0]);
		}
	}

	
	private int updatecounter = 0;
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
			updatecounter++;
			if (updatecounter > 4) {
				file.sendUpdate(pingmap.keySet());
				updatecounter = 0;
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
			byte[] b = new byte[] { id, seq, nextSpecSeq(), ack, ackid, 0 };
			Send(b);
		} finally {
			lock.unlock();
		}
	}

	public void sendPing() {
		lock.lock();
		try {
			byte[] b = new byte[] { id, seq, nextSpecSeq(), -2, -2, 0 };
			byte[] out = new byte[b.length + name.length];
			System.arraycopy(b, 0, out, 0, b.length);
			System.arraycopy(name, 0, out, b.length, name.length);
			Send(out);
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
			//byte[] toSend = sess.encryptPlainText(out, sess.getAuthKey()); // encrypt
			//DatagramPacket snd = new DatagramPacket(out, out.length, group, port);
			ackmap = new HashMap<>();
			for (Byte host : pingmap.keySet()) {
				ackmap.put(host, false);
			}
			byte re = 0;
			while (true) {
				Send(out);
				try {
					Thread.sleep(200);
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
				re++;
				out[2] = re;
				//snd = new DatagramPacket(newsnd, newsnd.length, group, port);
			}

		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e1) {
			System.out.println("Failed to encrypt payload");
		} finally {
			lock.unlock();
		}
	}

	public void Send(byte[] out) {
		try {
			//System.out.println("Non encrypted: " + new String(out));
			byte[] encrypted = new byte[out.length + password.length];
			System.arraycopy(password, 0, encrypted, 0, password.length);
			System.arraycopy(out, 0, encrypted, password.length, out.length);
			encrypted = sess.encryptPlainText(encrypted, sess.getAuthKey());
			byte[] toSend = new byte[encrypted.length + 1];
			toSend[0] = (byte) (toSend.length - 1);
			System.arraycopy(encrypted, 0, toSend, 1 , encrypted.length);
			//System.out.println("encrypted: " + (byte) (toSend.length - 1) + " " + (toSend.length - 1));
			DatagramPacket snd = new DatagramPacket(toSend, toSend.length, group, port);
				socket.send(snd);
		} catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Failed to send message");
		}
	}
	
	

}
