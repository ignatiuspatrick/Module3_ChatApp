package RoutingProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataTransferProtocol {
	private Integer[][] packets;
	private List<Integer> ack = new ArrayList<>();
	private RoutinProtocol networkLayer;
	static final int HEADERSIZE = 1; // number of header bytes in each packet
	static final int DATASIZE = 64; // max. number of user data bytes in each packet
	static final int TIMEOUT = 1000; // the timeout
	static final List<Integer> SEQ_NO = new ArrayList<>(Arrays.asList(0, 1)); // the sequence numbers used

	public void sender() {
        System.out.println("Sending...");

        // read from the input text message
        String[] message = ;
        
        // keep track of where we are in the data
        int msgPointer = 0;
        
        // calculate the amount of packets to send
        packets = new Integer[(int) Math.ceil((double) message.length / DATASIZE)][];
        // get the first sequence number
        int seq = SEQ_NO.get(0);
        
        // divide the file into packages and send them one by one
        for (int i = 0; i < packets.length; i++) {
	        	int datalen = Math.min(DATASIZE, message.length - msgPointer);
	        	packets[i] = new Integer[HEADERSIZE + datalen];
	        	packets[i][0] = seq;
	        	System.arraycopy(message, msgPointer, packets[i], HEADERSIZE, datalen);
	        	// checks for a duplicate acknowledgement, and retransmits it if it is true.
	        	if(this.cstvAck() == false) {
	        		this.send(packets[i]);
	        		msgPointer += datalen;
		        	seq = SEQ_NO.get(++seq % SEQ_NO.size());
	        	} else {
	        		this.send(packets[i-1]);
	        		msgPointer += datalen + packets[i].length - HEADERSIZE;
	        		seq = SEQ_NO.get(++seq % SEQ_NO.size());
	        	}
        }
        // send the final packet, indicating that the transfer has been completed
        this.send(new Integer[] {0});
    }

	public void send(Integer[] packet) {
		System.out.println("Send");
		boolean stop = false;
		while (!stop) {
			// send the packet
			getNetworkLayer().sendPacket(packet);
			System.out.println("Sent one packet with header: " + packet[0]);

			boolean timeout = false;
			long start = System.currentTimeMillis();
			long time = 0;
			Integer[] ackPacket = getNetworkLayer().receivePacket();
			if (ackPacket == null) {
				time = System.currentTimeMillis();
			}
			while (!timeout) {
				// if the recorded time is greater than the timeout, then send the packet again
				if (time - start >= TIMEOUT) {
					timeout = true;
				} else {
					// wait for the acknowledgement
					if (ackPacket != null && ackPacket[0] == packet[0]) {
						System.out.println("Ack received: " + ackPacket[0]);
						ack.add(ackPacket[0]);
						timeout = true;
						stop = true;
					}
				}
			}
		}
	}

	public void receiver() {
		System.out.println("Receiving...");

		// create the array that will contain the message contents
		// note: we don't know yet how large the file will be, so the easiest (but not
		// most efficient)
		// is to reallocate the array every time we find out there's more data
		Integer[] msgContents = new Integer[0];

		// sets the previous sequence number to -1
		int seq = -1;

		// loop until we are done receiving the file
		boolean stop = false;
		while (!stop) {

			// try to receive a packet from the network layer
			Integer[] packet = getNetworkLayer().receivePacket();

			// if we indeed received a packet
			if (packet != null) {
				// if the packet length is 1, then it indicates the end of the transfer
				if (packet.length == 1) {
					stop = true;
					// if the sequence number of the packet is the same as the previous one, then
					// discard the packet
					// and send an acknowledgement
				} else if (packet[0] == seq) {
					System.out.println("Received sequence number " + packet[0] + ". Discarding packet...");
					sendAck(packet[0]);
				} else {
					// tell the user
					System.out.println("Received packet, length=" + packet.length + "  first byte=" + packet[0]);

					// append the packet's data part (excluding the header) to the fileContents
					// array, first making it larger
					int oldlength = msgContents.length;
					int datalen = packet.length - HEADERSIZE;
					msgContents = Arrays.copyOf(msgContents, oldlength + datalen);
					System.arraycopy(packet, HEADERSIZE, msgContents, oldlength, datalen);
					this.sendAck(packet[0]);
					seq = packet[0];
				}
			} else {
				// wait ~10ms (or however long the OS makes us wait) before trying again
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					stop = true;
				}
			}
		}

		// write to the output file
		// Utils.setFileContents(fileContents, getFileID());
	}

	// send an acknowledgement to the sender
	public void sendAck(int seq) {
		System.out.println("Sent ack: " + seq);
		getNetworkLayer().sendPacket(new Integer[] { seq });
	}

	// check for a duplicate acknowledgement.
	public boolean cstvAck() {
		if (ack.size() >= 2 && ack.get(ack.size() - 1).compareTo(ack.get(ack.size() - 2)) == 0) {
			return true;
		} else {
			return false;
		}
	}

	protected NetworkLayer getNetworkLayer() {
		return networkLayer;
	}

}
