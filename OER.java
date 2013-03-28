//Dasun Wang and Thomas Yun

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * This class contains a set of shared resources numbers/constants/times to implement the optimized edge routing routine
 */

public abstract class OER extends Thread{

	//PriorityQueue<DatagramPacket> queue; //also keeps track of sequence #'s seen
	//LinkedList<DatagramPacket> queue;
	ArrayList<Long> sendTimes;
	ArrayList<Long> roundTripTimes;
	
	double timeout = 0;
	
	DatagramPacket ack;  //to receive packets on dest side
	final static double Q = 0.5; //constant
	final static double A0 = 500; //initial A0 value
	final static int A = 12;

	long arrivalTime;
	long queueTime;
	
	public ArrayList<Long> getSendTimes(){
		return sendTimes;
	}
	
	public ArrayList<Long> getRoundTripTimes(){
		return roundTripTimes;
	}
}
