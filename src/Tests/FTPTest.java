package Tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import RoutingProtocol.FileTransferProtocol;

class FTPTest {

	FileTransferProtocol file;
	FileTransferProtocol file2;
	MessageHolder h;
	MessageHolder k;
	
	@BeforeEach
	void setUp() throws Exception {
		file = new FileTransferProtocol( (byte) 1, "password", "bbb");
		file2 = new FileTransferProtocol( (byte) 2, "password", "aaa");
		h = new MessageHolder();
		k = new MessageHolder();
		file.addObserver(k);
		file2.addObserver(h);

	}

	@Test
	void NormalMessaging() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		assertTrue(h.getMessage() == null);
		assertTrue(h.getMessage() == null);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < 1; i++) {
			try {
				file.sendMessage("Hello World " + i);
				Thread.sleep(50);
				file2.sendMessage("Hello World " + i);
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			assertTrue(h.getMessage() != null);
			assertTrue(h.getMessage().toString().equals("bbb: Hello World " + i));
			assertTrue(h.getMessage().getTime() <= System.currentTimeMillis());
			
			assertTrue(k.getMessage() != null);
			assertTrue(k.getMessage().toString().equals("aaa: Hello World " + i));
			assertTrue(k.getMessage().getTime() <= System.currentTimeMillis());
		}
		file.getrouting().close();
		file2.getrouting().close();
	}
	
	@Test
	void Reconnect() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for (int k = 0; k < 1; k++) {
			for (int i = 0; i < 10; i++) {
				file.sendMessage("Hello World");
			}
			try {
				Thread.sleep(500);
				file.getrouting().close();
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			file = new FileTransferProtocol( (byte) 1, "password", "bbb");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			file.sendMessage("after reconnect");
			
			assertTrue(h.getMessage() != null);
			assertTrue(h.getMessage().toString().equals("bbb: after reconnect"));
			assertTrue(h.getMessage().getTime() <= System.currentTimeMillis());
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		file.getrouting().close();
		file2.getrouting().close();
	}

}
