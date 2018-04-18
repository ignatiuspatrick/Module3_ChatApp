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
	private boolean running = true;
	private final Thread ping;
	
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
		ping = new Thread(new PingThread(this));
		ping.start();
	}
	
	public void close() {
		ping.interrupt();
		running = false;
		socket.close();
	}

	// Listen for incoming messages
	public void run() {
		while (true) {
			byte[] buf = new byte[1000];
			byte[] recb = new byte[5];
			byte[] drev = new byte[5];
			boolean flag;
			// Receive message
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
				if (!running) {
					break;
				}
				byte[] temp = rec.getData();
				drev = new byte[temp[0]];
				System.arraycopy(temp, 1, drev, 0, temp[0]);;
				
				// decrypt with authKey
				try {
					drev = sess.decryptPlainText(drev, sess.getSecretkey());
					
				} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
					System.out.println("Failed to decrypt message");
					flag = true;
					continue;
				} 
				
				// Check if pass is correct
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
				
				// Put Message in new byte array
				if (drev[password.length + 3] == -1) {
					recb = new byte[HEADER_LENGTH + drev[password.length + 5] + 16];
					System.arraycopy(drev, password.length, recb, 0, HEADER_LENGTH + drev[password.length + 5] + 16);
				} else {
					recb = new byte[HEADER_LENGTH + drev[password.length + 5]];
					System.arraycopy(drev, password.length, recb, 0, HEADER_LENGTH + drev[password.length + 5]);
				}
			} while (recb[0] == id || flag == true);
			if (!running) {
				break;
			}
			// Ping from other computers.
			if (recb[3] == -2) {	
				if (!users.containsKey(recb[0]) || !pingmap.containsKey(recb[0])) {
					users.put(recb[0], new Byte[] { recb[1], recb[2], -1 });
					relayMessage(recb);
				}
				pingmap.put(recb[0], (byte) 5);
				byte[] n = new byte[recb[5]];
				System.arraycopy(recb, HEADER_LENGTH, n, 0, recb[5]);
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
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Failed to decrypt incoming payload");
			e.printStackTrace();
		}
	}

	// Relay messages to next nodes
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

	// to the next special sequence number
	public byte nextSpecSeq() {
		specseq++;
		if (specseq == 100) {
			specseq = 0;
		}
		return specseq;
	}

	// Send acks for received messages.
	public void sendAck(byte ack, byte ackid) {
		lock.lock();
		try {
			byte[] b = new byte[] { id, seq, nextSpecSeq(), ack, ackid, 0 };
			Send(b);
		} finally {
			lock.unlock();
		}
	}

	// Send a ping to let other nodes know our name
	public void sendPing() {
		lock.lock();
		try {
			byte[] b = new byte[] { id, seq, nextSpecSeq(), -2, -2, (byte) name.length };
			byte[] out = new byte[b.length + name.length];
			System.arraycopy(b, 0, out, 0, b.length);
			System.arraycopy(name, 0, out, b.length, name.length);
			Send(out);
		} finally {
			lock.unlock();
		}
	}

	// used by upper layer to send messages.
	public synchronized void outMessage(byte[] message) {
		lock.lock();
		try {
			SecretKey s = sess.generateSessionKey(); // generate session key
			message = sess.encryptPlainText(message, s); // encrypt plain message with session key
			byte[] b = new byte[] { id, nextSeq(), 0, -1, -1, (byte) message.length };
			byte[] out = new byte[message.length + b.length + s.getEncoded().length];
			System.arraycopy(b, 0, out, 0, b.length);
			System.arraycopy(message, 0, out, b.length, message.length);
			System.arraycopy(s.getEncoded(), 0, out, b.length + message.length, s.getEncoded().length);
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
			}

		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e1) {
			System.out.println("Failed to encrypt payload");
		} finally {
			lock.unlock();
		}
	}

	// Encrypt message before sending
	public void Send(byte[] out) {
		try {
			byte[] encrypted = new byte[out.length + password.length];
			System.arraycopy(password, 0, encrypted, 0, password.length);
			System.arraycopy(out, 0, encrypted, password.length, out.length);
			encrypted = sess.encryptPlainText(encrypted, sess.getSecretkey());
			byte[] toSend = new byte[encrypted.length + 1];
			toSend[0] = (byte) (toSend.length - 1);
			System.arraycopy(encrypted, 0, toSend, 1 , encrypted.length);
			DatagramPacket snd = new DatagramPacket(toSend, toSend.length, group, port);
				socket.send(snd);
		} catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Failed to send message");
		}
	}
	
	

}
