package RoutingProtocol;

import java.net.DatagramPacket;

public class PacketDivision {
	private byte[][] packets;
	private byte[] lastPacket = new byte[] {-1};
	private RoutingProtocol routingProtocol;
    private static final int HEADERSIZE=1;   // number of header bytes in each packet
    private static final int DATASIZE=64;   // max. number of user data bytes in each packet
    
    public void receiver(DatagramPacket[] message) {
        System.out.println("Receiving...");

        // loop until we are done receiving the file
        boolean stop = false;
        while (!stop) {
            // if we indeed received a packet
            if (message != null) {
            		stop = true;
            		// keep track of where we are in the data
                int msgPointer = 0;
                
                // calculate the amount of packets to send
                packets = new byte[(int) Math.ceil((double) message.length / DATASIZE)][];
                // get the first sequence number
                int seq = 1;
                
                // divide the file into packages and send them one by one
                for (int i = 0; i < packets.length; i++) {
	        	        	int datalen = Math.min(DATASIZE, message.length - msgPointer);
	        	        	packets[i] = new byte[HEADERSIZE + datalen];
	        	        	packets[i][0] = (byte) seq;
	        	        	System.arraycopy(message, msgPointer, packets[i], HEADERSIZE, datalen);
	        	        	// checks for a duplicate acknowledgement, and retransmits it if it is true.
        	        		this.sender(packets[i]);
        	        		msgPointer += datalen;
        		        	seq += 1;
                } 
                // send the final packet, indicating that the transfer has been completed
                this.sender(lastPacket);
            }
        }
    }
    
    // send each packet coming from receiver() to routing protocol in order
    public void sender(byte[] packet) {
        System.out.println("Sending...");
        routingProtocol.outMessage(packet);
    }

}
