package Agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

	public void checkLocation() {

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
			reader = new BufferedReader(new FileReader("/home/redvox/eclipse_workspace/vsp1/t.txt"));
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
