package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

	private ServerSocket welcomeSocket;
	private int maxConnections = 1;
	private Integer connections = 0;
	private final String BUSY = "busy";
	private final String OK = "ok";
	
	private String successorFileLocation;
	private String dateFileLocation;

	public static void main(String[] args) {
		Server server = new Server();
		server.setMaxConnections(100);
		server.listen(8080);
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
					SocketThread conn = new SocketThread(clientSocket);
					conn.start();
					synchronized (connections) {
						connections++;
					}
					System.out.println("Client connected!");
				} else {
					//Busy anzeigen
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
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

//						if (message.equals(exit)) {
//							quit = true;
//							System.out.println("Client disconnected.");
//							break;
//						}

						pw.println(message);
						cbuf = new char[255];

						if (message.equals("STAT")) {

//							pw.println("Connected Clients: " + connections + ", Messages: " + messages + ", Overall Messages: " + allMessages + ", Session time: " + clientUptime / 1000 + " sec, Server has been up for " + hours + "  hours, " + minutes + " minutes and " + seconds + " seconds.");
						} else {

//							System.out.println("Message(" + messages + ") received: '" + message + "' from " + clientId + " (" + clientSocket.getInetAddress() + ":" + clientSocket.getLocalPort() + ")(" + serverUptime + "ms): ");

//							System.out.println("Send message: '" + message + "' to " + clientId + " (" + clientSocket.getInetAddress() + ":" + clientSocket.getLocalPort() + " (" + serverUptime + "ms): ");
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
}
