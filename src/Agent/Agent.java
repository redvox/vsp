package Agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Server.Server;

public class Agent extends Thread {

	List<String> successorlist = new ArrayList<String>();
	Map<String, String> datalist = new HashMap<String, String>();
	String homeadress;
	Server server;

	public Agent(String homeadres) {
		this.homeadress = homeadres;
	}

	/**
	 * On arrival, the agend has to check its current loacation.
	 * If he has no access to the serves interface list he return home.
	 * If the server he is on its not his actual target, he will attempt to travel to his target.
	 * If he is on his target he must verify if he is on his homeserver and display the collecting data.
	 * If the server is a normal target he will try to read the succsessor and data an procede as normally. 
	 */
	public void checkLocation() {
		List<String> iplist = getIPAddressList();
		// no interface access
		if(!iplist.isEmpty()){
			// actual target
			if(iplist.contains(successorlist.get(successorlist.size()-1))){
				//on home server
				if(homeadress.equals(successorlist.size()-1)){
					
					
				} else {
					// procede as normally
				}
			} else {
				// travel weiter
			}
		} else {
			// return home
			successorlist.add(homeadress);
		}
	}
	
	public static List<String> getIPAddressList() {
		List<String> iplist = new ArrayList<String>();
		
		Enumeration nicList;
		NetworkInterface nic;
		Enumeration nicAddrList;
		InetAddress nicAddr;
		try {
			nicList = NetworkInterface.getNetworkInterfaces();
			while (nicList.hasMoreElements()) {
				nic = (NetworkInterface) nicList.nextElement();
				if (!nic.isLoopback() && nic.isUp()) {
					nicAddrList = nic.getInetAddresses();
					while (nicAddrList.hasMoreElements()) {
						nicAddr = (InetAddress) nicAddrList.nextElement();
						try {
							// test if it's IPv4, if doesn't throws Exception.
							Inet4Address nicAddrIPv4 = (Inet4Address) nicAddr;
							iplist.add(nicAddr.getHostAddress());
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (SocketException e1) {
			System.out.println("SocketException handled in Networking.getIPAddress!.");
		}
		return iplist;
	}

	/**
	 * readSuccsessor tries to read the Successor File and checks each entry
	 * whether it is an IP address and ignore it if its not. If the List with
	 * new IPs is empty or the file could not read, the agend set his homeadress
	 * as its next target.
	 */
	public void readSuccessor() {
		BufferedReader reader;
		String zeile = null;
		boolean returnhome = false;

		ArrayList<String> ips = new ArrayList<String>();

		try {
			reader = new BufferedReader(new FileReader(server.getSuccessorFileLocation()+"t.txt"));
			zeile = reader.readLine();

			// Get every entry in the file.
			ArrayList<String> values = new ArrayList<String>();

			while (zeile != null) {
				String[] t = zeile.split(";");
				for (int i = 0; i < t.length; i++) {
					values.add(t[i]);
				}
				zeile = reader.readLine();
			}

			// Check if entry is an IP.
			for (String s : values) {
				String[] t = s.split("\\.");
				if (t.length == 4) {
					try {
						int ip0 = Integer.parseInt(t[0]);
						int ip1 = Integer.parseInt(t[1]);
						int ip2 = Integer.parseInt(t[2]);
						int ip3 = Integer.parseInt(t[3]);
						if (ip0 <= 255 && ip0 >= 0) {
							if (ip1 <= 255 && ip1 >= 0) {
								if (ip2 <= 255 && ip2 >= 0) {
									if (ip3 <= 255 && ip3 >= 0) {
										ips.add(s);
									}
								}
							}
						}
					} catch (NumberFormatException nfe) {
						// Ignore entry.
					}
				}
			}

			if (ips.isEmpty())
				returnhome = true;

		} catch (IOException e) {
			returnhome = true;
		}

		if (returnhome && successorlist.isEmpty()) {
			successorlist.add(homeadress);
		}
	}

	public void checkSuccessor() {

	}

	public void readData() {

	}

	public void doSendRequest() {

	}
}
