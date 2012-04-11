package Server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Take extends Thread {

	ServerSocket toAgent;
	Socket socketAgent;
	int port = 7777;

	public void run() {
		while (true) {

			toAgent = new ServerSocket(port);
			socketAgent = toAgent.accept();

			System.out.println(InetAddress.getLocalHost().getHostName() + " ist mit Agent verbunden!");

			InputStream inputS = socketAgent.getInputStream();

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
			MyObjectInputStream objectDESERIALISIEREN = new MyObjectInputStream(loaderMA, socketAgent.getInputStream());

			Object newMobileAgent = objectDESERIALISIEREN.readObject();

			System.out.println("MobileAgent konnte erfolgreich deserialisiert werden!");

			// MA starten
			// Methode aufrufen, deren Name zur Laufzeit als Text bekannt ist.
			// (Reflektion)
			Method run = newMobileAgent.getClass().getMethod("run", null);
			run.invoke(newMobileAgent, null);

			// Streams schließen
			objectDESERIALISIEREN.close();
			inputS.close();
			socketAgent.close();
			toAgent.close();
		}
	}
}
