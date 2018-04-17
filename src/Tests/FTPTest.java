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
		file = new FileTransferProtocol( (byte) 1, "password", "name");
		file2 = new FileTransferProtocol( (byte) 1, "password", "name");
	}

	@Test
	void test() {
	
	}

}
