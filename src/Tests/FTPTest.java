package Tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import RoutingProtocol.FileTransferProtocol;

class FTPTest {

	FileTransferProtocol file;
	FileTransferProtocol file2;
	
	@BeforeEach
	void setUp() throws Exception {
		file = new FileTransferProtocol( (byte) 1, "password", "bbb");
		file2 = new FileTransferProtocol( (byte) 2, "password", "aaa");
	}

	@Test
	void test() {
		MessageHolder h = new MessageHolder();
		file2.addObserver(h);
		
		assertTrue(h.getMessage() == null);
		try {
			Thread.sleep(500);
			file.sendMessage("Hello World");
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(h.getMessage() != null);
		assertTrue(h.getMessage().toString().equals("bbb: Hello World"));
		assertTrue(h.getMessage().getTime() <= System.currentTimeMillis());
	}
	
	@Test
	void test2() {
		for (int i = 0; i < 1000; i++) {
			file.sendMessage("Hello");
			file2.sendMessage("Hello2");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	

}
