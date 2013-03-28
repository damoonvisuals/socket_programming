//Dasun Wang and Thomas Yun

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.*;

public class Destination extends OER {

	DatagramSocket destination;
	InetAddress destAddress;
	LinkedList<DatagramPacket> queue;
	boolean flag = false;
	int destPortNum;
	
	ExecutorService destThread;
	
	public Destination(InetAddress sAddress) {
		// INITIALIZATION
		try {
			destAddress = sAddress; //sets it so this program can run on the same local machine with different ports
			destPortNum = 5553;
			//destAddress = InetAddress.getLocalHost();
			// System.out.println(destAddress);

			destThread = Executors.newFixedThreadPool(4);
			destination = new DatagramSocket(destPortNum, destAddress);

			// System.out.println("destportnum: " + destination.getLocalPort() +
			// " destaddress: " + destination.getLocalAddress());
			System.out.println("Destination socket created");
			listen();
			queueThread();
//			queue = new PriorityQueue<DatagramPacket>(); // initializes empty
//															// queue
			queue = new LinkedList<DatagramPacket>();
			System.out.println("Queue created");

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public LinkedList<DatagramPacket> getQueue(){
		return queue;
	}
	
	// called after source sends packet
	public void accept(DatagramPacket request) {

		final DatagramPacket pack = request;
		// queue in its own thread
		// System.out.println(queue == null);

		queue.add(pack); // add the sent packet to the queue
		queueTime = Qn(A);
		
		// debugging
		String pinfo = new String(pack.getData());
		System.out.println("Packet " + pinfo + " added to queue");

		ack = new DatagramPacket(new byte[pack.getData().length],
				pack.getData().length);
	}
	
	public void queueThread() {
		destThread.execute(new Runnable() {

			public void run() {
				while (true) {
					if (queue.isEmpty()) {
						while (queue.isEmpty()) {
							arrivalTime = System.currentTimeMillis();
						}
					}
					if (System.currentTimeMillis() > (arrivalTime + queueTime)) {
						ack = queue.getFirst();
						queue.remove();
						System.out.println(System.currentTimeMillis() + " Delayed");
						System.out.println(arrivalTime + " ArrivalTime");
						System.out.println(queueTime + " Q (random)");
						System.out.println(arrivalTime + queueTime + " New");

						arrivalTime = System.currentTimeMillis();
						queueTime = Qn(A); //Q distributed by A
						flag = true;
					}
					/*
					 * while (!queue.isEmpty()) { //Thread.sleep(qExponent(A));
					 * queue.poll(); // removes front of queue and defines
					 * packet to be sent to dest flag = true; transTime =
					 * System.currentTimeMillis(); }
					 */
				}
			}
		});
	}

	public static int Qn(int a) {
		Random randGenerator = new Random();
		return randGenerator.nextInt((int) Math.pow(2, a));
	}

	// run after destination socket is started, starts Listening for packets
	// from source
	public void listen() {
		// start new thread for Dest Listening, initialized once
		destThread.execute(new Runnable() {
			@Override
			public void run() {

				while (true) {
					if (flag) { // after queue waits exponential delay, dest listener is open
						try {
							System.out.println("Destination is listening");
							destination.receive(ack);
							// used for debugging
							String packetData = new String(ack.getData());
							System.out.println("Queue sending packet "
									+ packetData + " to dest");

							// change fields so it will send back to source
							ack.setPort(ack.getPort());
							ack.setAddress(ack.getAddress());
							
							// DatagramPacket sendPacket = new
							// DatagramPacket(ack.getData(), ack.getLength(),
							// sourceAddress, sourcePortNum);

							// System.out.println("ACK data: " + new
							// String(ack.getData()));
							// System.out.println("ACK port: " + ack.getPort());
							// System.out.println("ACK address: " +
							// ack.getAddress());

							System.out.println("Dest sending ACK for packet " + packetData);
							destination.send(ack); // sending back the ack
							

							ack = new DatagramPacket(new byte[256], 256); // clears ack for next packet
							flag = false; // stops it from listening
							Thread.sleep(1);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
		});

	}

	public InetAddress getDestAddress() {
		// TODO Auto-generated method stub
		return destAddress;
	}
	
	public int getDestPortNum(){
		return destPortNum;
	}
}