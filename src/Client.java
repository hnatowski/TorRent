import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

public class Client {
	User user;
	BufferedReader in;
	PrintWriter out;
	int id;
	ServerSocket server = null;
	Socket client = null;


	public void listenSocket() throws UnknownHostException, NoSuchAlgorithmException, IOException {

		try {
			server = new ServerSocket(0);
		} catch (IOException e) {
			System.out.println("Could not listen");
			System.exit(-1);
		}
		System.out.println("Peer listens on port: " + server.getLocalPort());
		start();
		user = new User(id, server);

		new Thread(user).start();

		while (true) {
			try {

				client = server.accept();
				
				if (client.getInetAddress().toString().equals("/0:0:0:0:0:0:0:1")) {
					Date today = new Date();
					String l = new String(Files.readAllBytes(Paths.get(Config.Dir + "_" + id+"\\"+"log.txt")));
					String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + 
					"<!DOCTYPE html><html><body><h1>Client "+id+" log</h1><p>"+l+"</p></body></html>";
					client.getOutputStream().write(httpResponse.getBytes("UTF-8"));
				}

			} catch (IOException e) {

				System.out.println("Accept failed");
				System.exit(-1);
			}

			(new ClientThread(client, server, id)).start();
		}

	}

	public static void main(String[] args) throws UnknownHostException, NoSuchAlgorithmException, IOException {
		Client client = new Client();
		client.listenSocket();
	}

	public void start() throws UnknownHostException, IOException, NoSuchAlgorithmException {
		Socket socket = new Socket(Config.adress, Config.SPort);
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		out.println(999);
		id = Integer.parseInt(in.readLine());
		File folder = new File(Config.Dir + "_" + id);
		File[] listoffiles = folder.listFiles();
		String msg = "";
		if (folder.exists()) {
			for (File file : listoffiles) {
				if (file.isFile()) {
					byte[] buffer = new byte[10000];
					MessageDigest md = MessageDigest.getInstance("MD5");
					FileInputStream fis = new FileInputStream(file);
					DigestInputStream dis = new DigestInputStream(fis, md);
					try {
						while (dis.read(buffer) != -1)
							;
					} finally {
						dis.close();
					}

					msg += file.getName() + ";" + DatatypeConverter.printHexBinary(md.digest()) + ";";
				}
			}
			out.println("start" + id + ";" + msg + ";" + server.getInetAddress().getHostAddress() + ";"
					+ server.getLocalPort());
		}

		else {
			System.out.println("Folder " + Config.Dir + "_" + id + " nie istnieje. Utworzy³em nowy folder.");
			(new File(Config.Dir + "_" + id)).mkdir();

			out.println("start" + id + ";" + server.getInetAddress().getHostAddress() + ";" + server.getLocalPort());
		}
	}

	public String getMD5(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest md;
		md = MessageDigest.getInstance("MD5");
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		
		ByteBuffer buff = ByteBuffer.allocate(2048);
		while (channel.read(buff) != -1) {
			buff.flip();
			md.update(buff);
			buff.clear();
		}
		byte[] hashValue = md.digest();
		return new String(hashValue);
	}
}