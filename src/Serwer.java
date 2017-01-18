import java.io.*;
import java.net.*;


public class Serwer {
	public void listenSocket() throws IOException {
		ServerSocket server = null;
		Socket client = null;
		try {
			server = new ServerSocket(Config.SPort); 
		}
		catch (IOException e) {
			System.out.println("Could not listen");
			System.exit(-1);
		}
		System.out.println("Server listens on port: " + server.getLocalPort());
		
		while(true) {
			try {
				client = server.accept();
			}
			catch (IOException e) {
				System.out.println("Accept failed");
				System.exit(-1);
			}
		
			(new ServerThread(client)).start();
		}
		
	}

	public static void main(String[] args) throws IOException {
		Serwer server = new Serwer();
		server.listenSocket();
	}
}