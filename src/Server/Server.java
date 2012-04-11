package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import Agent.Agent;

public class Server {

	private ServerSocket welcomeSocket;
	private int maxConnections = 1;
	private Integer connections = 0;
	private static int port = 7777;
	private final String BUSY = "busy";
	private final String OK = "ok";

	private String successorFileLocation = "/home/redvox/eclipse_workspace/vsp1/";
	private String dataFileLocation = "/home/redvox/eclipse_workspace/vsp1/";

	public static void main(String[] args) {

		Agent a;
		
		try {
			// extrahiere Binärcode der Klasse MobileAgent
//			File mobileAgentDATEI = new File("./bin/Agent.class");
			File mobileAgentDATEI = new File("/home/redvox/eclipse_workspace/vsp1/bin/Agent/Agent.class");
			
			byte[] binaercodeMA = new byte[(int) mobileAgentDATEI.length()];
			FileInputStream fileIS = new FileInputStream(mobileAgentDATEI);
			fileIS.read(binaercodeMA, 0, binaercodeMA.length);

			fileIS.close();
		
			a = new Agent(InetAddress.getLocalHost(), binaercodeMA);
			a.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Server server = new Server();
		server.setMaxConnections(100);
		server.listen(port);
	}

	public Server() {

	}

	public void setMaxConnections(int i) {
		maxConnections = i;
	}

	public void listen(int port) {
		try {
			welcomeSocket = new ServerSocket(port);
			System.out.println("Waiting for a connection on " + port);

			while (true) {
				if (connections < maxConnections) {

					Socket clientSocket = welcomeSocket.accept();
					// SocketThread conn = new SocketThread(clientSocket);
					// conn.start();
					acceptAgent(clientSocket);
					synchronized (connections) {
						connections++;
					}
					System.out.println("Client connected!");
				} else {
					// Busy anzeigen
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void acceptAgent(Socket clientSocket) throws IOException {
		InputStream inputS = clientSocket.getInputStream();

		// Größe der Klasse MA empfangen
		String tmpSizeMA = "";
		char tmpChar;
		while ((tmpChar = (char) inputS.read()) != ';') {
			tmpSizeMA += String.valueOf(tmpChar);
		}
		int sizeMA = Integer.parseInt(tmpSizeMA);

		// Binaercode der Klasse empf./einlesen
		byte[] binaercode = new byte[sizeMA];
		inputS.read(binaercode, 0, sizeMA);

		// Klasse MA in der Virtual Machine erzeugen
		ClassLoaderMobileAgent loaderMA = new ClassLoaderMobileAgent();
		loaderMA.defineClass(binaercode, sizeMA);

		// MA deserialisieren mit dem ClassLoader für MobileAgent
		MyObjectInputStream objectDESERIALISIEREN = new MyObjectInputStream(loaderMA, clientSocket.getInputStream());

		try {

			Object newMobileAgent = objectDESERIALISIEREN.readObject();

			System.out.println("MobileAgent konnte erfolgreich deserialisiert werden!");

			// MA starten
			// Methode aufrufen, deren Name zur Laufzeit als Text bekannt ist.
			// (Reflektion)
			Method run = newMobileAgent.getClass().getMethod("run", null);
			run.invoke(newMobileAgent, null);

		} catch (Exception ex) {
			// Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null,
			// ex);
		}

		// Streams schließen
		objectDESERIALISIEREN.close();
		inputS.close();
		clientSocket.close();
		// toAgent.close();
	}

	private class SocketThread extends Thread {

		private Socket clientSocket;
		private int timeout = 10000;

		public SocketThread(Socket s) {
			clientSocket = s;
		}

		@Override
		public void run() {
			String message;
			boolean quit = false;

			try {
				clientSocket.setSoTimeout(timeout);
				PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				char[] cbuf = new char[512];
				while (true) {
					if (clientSocket.isClosed() || quit)
						break;

					while (br.read(cbuf) != -1) {
						message = String.valueOf(cbuf);
						message = message.toUpperCase().trim();

						// if (message.equals(exit)) {
						// quit = true;
						// System.out.println("Client disconnected.");
						// break;
						// }

						pw.println(message);
						cbuf = new char[255];

						if (message.equals("STAT")) {

							// pw.println("Connected Clients: " + connections +
							// ", Messages: " + messages +
							// ", Overall Messages: " + allMessages +
							// ", Session time: " + clientUptime / 1000 +
							// " sec, Server has been up for " + hours +
							// "  hours, " + minutes + " minutes and " + seconds
							// + " seconds.");
						} else {

							// System.out.println("Message(" + messages +
							// ") received: '" + message + "' from " + clientId
							// + " (" + clientSocket.getInetAddress() + ":" +
							// clientSocket.getLocalPort() + ")(" + serverUptime
							// + "ms): ");

							// System.out.println("Send message: '" + message +
							// "' to " + clientId + " (" +
							// clientSocket.getInetAddress() + ":" +
							// clientSocket.getLocalPort() + " (" + serverUptime
							// + "ms): ");
						}

					}
				}
				pw.close();
				br.close();
				// sockets.remove(clientId);
				clientSocket.close();
				synchronized (connections) {
					connections--;
				}
			} catch (SocketTimeoutException t) {
				System.out.println("Client disconnected!");
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
				try {
					clientSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
	}

	public String getSuccessorFileLocation() {
		return successorFileLocation;
	}

	public String getDataFileLocation() {
		return dataFileLocation;
	}
}
