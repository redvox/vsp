package main;

import java.awt.font.NumericShaper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.omg.PortableInterceptor.SUCCESSFUL;

public class main {

	public Inet4Address homeadress;
	public List<InetAddress> successorlist = new ArrayList<InetAddress>();
	List<InetAddress> unreachable = new ArrayList<InetAddress>();
	
	public static void main(String[] args) {
			
//		main m = new main();
//		m.readSuccessor();
//		
//		for(InetAddress i : m.successorlist){
//			System.out.println(i.getHostName());
//			System.out.println(i.getHostAddress());
//			System.out.println(i.getCanonicalHostName());
//		}
//		try {
//			InetAddress i = InetAddress.getByName("127.0.0.1");
//			System.out.println(i.getHostName());
//			System.out.println(i.getHostAddress());
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
			System.out.println(InetAddress.getByName( "NeNeNe" ).isReachable( 2000 ));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
							interrupt();
						}

					}
				} catch (Exception e) {
					// ignore exceptions. the agend have to travel to the homeserver(!).
					
					try {
						sleep(2000);
					} catch (InterruptedException e2) {
						e2.printStackTrace();
						interrupt();
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
}