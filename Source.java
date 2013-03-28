//Dasun Wang and Thomas Yun

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Source extends OER {

	DatagramSocket source;
	Destination dest;
	int sourcePortNum;
	InetAddress sourceAddress;
	int packetID;
	
	public Source() {
		// INITIALIZATION
		try {
			//IntervalTimer ping = new IntervalTimer();
			sourceAddress = InetAddress.getLocalHost();
			sourcePortNum = 4321;
			
			source = new DatagramSocket(sourcePortNum, sourceAddress);
			// System.out.println("sourceportnum: " + source.getLocalPort() +
			// " sourceaddress: " + source.getLocalAddress());
			source.setReuseAddress(true); // allows use of same host addresses
			System.out.println("Source socket created");

			// creates destination D which starts listening for packet
			dest = new Destination(sourceAddress);
			//			dest.start();

			 //ping.start();
			 //InetAddress.getByName(sourceAddress.toString()).isReachable(5000);
			 //initialRTT = (int) ping.elapsed();
			 //ping = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sendTimes = new ArrayList<Long>();
		roundTripTimes = new ArrayList<Long>();

	}

	public InetAddress getSourceAddress(){
		return sourceAddress;
	}
	
	public int getSourcePortNum(){
		return sourcePortNum;
	}
	
	public void run() {
		ExecutorService MainThread = Executors.newFixedThreadPool(2);

		// THREADS and Sends a UDP packet every second and starts the Timer
		MainThread.execute(new Runnable() {
			@Override
			public void run() {
				byte[] b;
				DatagramPacket packet;
				IntervalTimer interval = new IntervalTimer();
				interval.start();

				while (true) {
					if (interval.elapsed() > 1000) {
						interval.reset();
						interval.start();
						// System.out.println(interval.elapsed());
						try {
							b = ((Integer) packetID).toString().getBytes(); // change packetID type to byte[]
							packet = new DatagramPacket(b, b.length, dest.getDestAddress(), dest.getDestPortNum());
							
							ack = new DatagramPacket(new byte[b.length], b.length); // creates buffer size for ack
							source.send(packet);

							String packetInfo = new String(packet.getData());
							System.out.println("Source sending packet " + packetInfo + " to queue");

							//show queue length
							System.out.println("Queue length at this point is  " + dest.getQueue().size());
							
							//when source sends packet is stores the time
							sendTimes.add(packetID, System.currentTimeMillis());

							packetID++; // increases packetID for next packet to be sent

							// the queue is processed in this statement in Destination.java
							dest.accept(packet);
							Thread.sleep(1);

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		});

		MainThread.execute(new Runnable() {

			@Override
			public void run() {
				DatagramPacket rcv; // buffer packet that holds D -> S
				
				// to receive packets on source side
				rcv = new DatagramPacket(new byte[256], 256);

				double An = A0; //initialRTT

				
				while (true) {
					// receive UDP packets
					// listening for D->S ACK, method blocks until it is heard
					System.out.println("Source is listening");
					try {
						source.receive(rcv);
						System.out.println("Source received ACK for packet " + new String(rcv.getData()).trim());
						
						//System.out.println("PacketData " + new String(rcv.getData()));
						int ACKID = Integer.parseInt(new String(rcv.getData()).trim()); // returns the sent data in integer form
						long rtt = System.currentTimeMillis() - sendTimes.get(ACKID); // compares current time to when that packet was sent
						
						//when source receives ACK it notes delay (rtt)
						System.out.println(System.currentTimeMillis() + " ReceiveTime");
						System.out.println(sendTimes.get(ACKID) + " SendTime");
						System.out.println("RoundTripTime: " + rtt + " milliseconds");
						
						timeout = avgRTT((int) rtt, An, Q) * 2; // An*2
						An = timeout / 2;

						System.out.println("Timeout: " + timeout);
						System.out.println("Average value (formula): " + An);
						
						/*int s=0;
						
						if(roundTripTimes.size() == 0){
							s = (int) A0; //first packet
							System.out.println("Average value (avg all rtt): " + s);
						}
						else {
							for(int i = 0; i < roundTripTimes.size(); i++){
								s += roundTripTimes.get(i);
							}
							System.out.println("Average value (avg all rtt): " + (s / roundTripTimes.size()));
						}*/
						
						

						// process the UDP packets
						// checks to see if ACK packet acknowledges a previous and if RTT > timeout
						if (rtt > timeout) {
							// discard packet and stop timer for that packet
							System.out.println("Packet Timeout");
							rcv = new DatagramPacket(new byte[256], 256); // clears rcv for next packet
						} else {
							while (ACKID > roundTripTimes.size()) {
								roundTripTimes.add((long) 0); //in case an ACK was missed fill in that packet index with 0
							}
							roundTripTimes.add(ACKID, rtt); // puts the calculated rtt in the appropriate index
							rcv = new DatagramPacket(new byte[256], 256); // clears rcv for next packet
						}
						
						
						//calculating average delay currently
						int avg = 0;
						for(int a = 0; a < roundTripTimes.size(); a++){
							avg += roundTripTimes.get(a);
						}
						System.out.println("Average delay up to this point is " + (avg/roundTripTimes.size()));
						
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	public double avgRTT(int currentRTT, double An, double q) {
		return (1 - q) * An + q * currentRTT;
	}

	public static void main(String[] args) throws IOException {
		// starts the source and transfer
		new Source().start();

	}

}