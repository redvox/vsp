package Agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import MobileAgent_Server.MobileAgent;
import Server.Server;

public class Agent extends Thread implements Serializable {

	private static final long serialVersionUID = 1L;

	List<InetAddress> successorlist = new ArrayList<InetAddress>();
	List<InetAddress> unreachable = new ArrayList<InetAddress>();
	List<InetAddress> visited = new ArrayList<InetAddress>();
	int id;
	int initTime;
	int port;
	Map<String, String[]> collectedData = new HashMap<String, String[]>();
	Inet4Address homeadress;
	Server server;
	
	byte[] agentbinarcode;

	public Agent(Inet4Address homeadres) {
		this.homeadress = homeadres;
	}

	public void run() {
		checkLocation();
	}

	/**
	 * On arrival, the agent has to check its current loacation. If he has no
	 * access to the serves interface list he return home. If the server he is
	 * on its not his actual target, he will attempt to travel to his target. If
	 * he is on his target he must verify if he is on his homeserver and display
	 * the collecting data. If the server is a normal target he will try to read
	 * the succsessor and data an procede as normally.
	 */
	public void checkLocation() {
		List<String> iplist = getIPAddressList();

		if (successorlist.isEmpty()) {
			// the agent is on his home server he has to read the succsessor and
			// travel.
			readSuccessor();
			doSendRequest();
		} else {
			if (iplist.contains(successorlist.get(successorlist.size() - 1))) {
				// actual target

				if (homeadress.equals(successorlist.size() - 1)) {
					// the agent is returned to his homeserver
					printData();

				} else {
					// procede as normally

					InetAddress location = successorlist.get(successorlist.size() - 1);
					successorlist.remove(successorlist.size() - 1);
					visited.add(location);

					readSuccessor();
					readData();
					checkSuccessor();
					doSendRequest();
				}
			} else {
				// the agent is not on his actual target, he tries to travel to
				// his target
				checkSuccessor();
				doSendRequest();
			}
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
		// boolean returnhome = false;

		ArrayList<InetAddress> ips = new ArrayList<InetAddress>();

		try {
			reader = new BufferedReader(new FileReader(server.getSuccessorFileLocation() + "t.txt"));
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
										try {
											ips.add(InetAddress.getByName(s));
										} catch (UnknownHostException e) {
											// Ignore entry.
										}
									}
								}
							}
						}
					} catch (NumberFormatException nfe) {
						// Ignore entry.
					}
				}
			}

		} catch (IOException e) {
			successorlist.add(homeadress);
		}

		successorlist.addAll(ips);
	}

	public void checkSuccessor() {
		for (InetAddress u : unreachable) {
			// the agent should check every host that was not reachable in the
			// past and add it again if it is reachable.
			try {
				if (u.isReachable(2000)) {
					unreachable.remove(u);
					successorlist.add(u);
				}
			} catch (Exception e) {
				unreachable.remove(u);
			}
		}

		boolean keeptrying = true;
		while (keeptrying) {
			if (successorlist.isEmpty()) {
				// if the agent has nowhere to go, return home.
				successorlist.add(homeadress);
			}

			if (successorlist.get(successorlist.size() - 1).equals(homeadress)) {
				// the target IS the home adress but the Server is not
				// reachable. The Agent must(!) wait until it is reachable.
				try {
					if (successorlist.get(successorlist.size() - 1).isReachable(2000)) {
						keeptrying = false;
					} else {

						try {
							sleep(2000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
							// interrupt();
						}

					}
				} catch (Exception e) {
					// ignore exceptions. the agend have to travel to the
					// homeserver(!).

					try {
						sleep(2000);
					} catch (InterruptedException e2) {
						e2.printStackTrace();
						// interrupt();
					}

				}
			} else {
				try {
					if (successorlist.get(successorlist.size() - 1).isReachable(2000)) {
						keeptrying = false;
					} else {
						unreachable.add(successorlist.get(successorlist.size() - 1));
						successorlist.remove(successorlist.size() - 1);
					}

				} catch (Exception e) {
					successorlist.remove(successorlist.size() - 1);
				}
			}
		}
	}

	public void readData() {
		try {
			File collectableData = new File("./nimm_dies_mit");
			if (collectableData.exists()) {
				collectedData.put("" + InetAddress.getLocalHost().getHostName() + "; " + InetAddress.getLocalHost().getAddress(), collectableData.list());
			} else {
				collectedData.put("" + InetAddress.getLocalHost().getHostName() + "; " + InetAddress.getLocalHost().getAddress(), new String[] { "no data found" });
			}

		} catch (UnknownHostException ex) {
			// Logger.getLogger(MobileAgent.class.getName()).log(Level.SEVERE,
			// null, ex);
		}
	}

	public void doSendRequest() {
		// Connection
		Socket toServer = null;
		OutputStream outputS;

		InetAddress target = successorlist.get(successorlist.size() - 1);

		// Setup Connection
		try {
			toServer = new Socket(target, port);
		} catch (Exception ex) {
			
		}

		// send
		try {

			outputS = toServer.getOutputStream();

			// Groeße des binaercodeMA
			String sizeBinaercodeMA = agentbinarcode.length + ";";
			outputS.write(sizeBinaercodeMA.getBytes());
			outputS.flush();

			// den binaercodeMA
			outputS.write(agentbinarcode);
			outputS.flush();

			// sich selbst :)
			ObjectOutputStream objectSERIALISIEREN = new ObjectOutputStream(toServer.getOutputStream());
			objectSERIALISIEREN.writeObject(this);
			objectSERIALISIEREN.flush();

			// Streams schliessen
			objectSERIALISIEREN.close();
			outputS.close();
			toServer.close();
			
		} catch (IOException ex) {
//			Logger.getLogger(MobileAgent.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NullPointerException ex) {
//			System.out.println("Agent hatte wohl Startprobleme ;)!");
		}
	}

	public void printData() {

	}
	
	public void p(String p){
		System.out.println(p);
	}
}
