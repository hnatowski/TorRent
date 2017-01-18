import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.bind.DatatypeConverter;

import org.omg.Messaging.SyncScopeHelper;

public class ClientThread extends Thread {
	private Socket socket;
	BufferedReader in;
	PrintWriter out;
	User user;
	ServerSocket server;
	int id;
	File log;
	PrintWriter pw;

	public ClientThread(Socket socket, ServerSocket server, int id)
			throws UnknownHostException, IOException, NoSuchAlgorithmException {
		super();
		this.socket = socket;
		this.server = server;
		this.id = id;

	}

	public void update() throws UnknownHostException, IOException, NoSuchAlgorithmException {
		Socket socket = new Socket(Config.adress, Config.SPort);

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		out.println(1);
		out.println("Update");
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
			out.println(id + ";" + msg + ";" + server.getInetAddress().getHostAddress() + ";" + server.getLocalPort());
		}

		else {
			System.out.println("Folder " + Config.Dir + "_" + id + " nie istnieje. Utworzy³em nowy folder.");
			(new File(Config.Dir + "_" + id)).mkdir();

			out.println("start" + id + ";" + server.getInetAddress().getHostAddress() + ";" + server.getLocalPort());
		}
	}

	public void run() {
		try {
			File log = new File(Config.Dir + "_" + id + "\\" + "log.txt");
			pw = new PrintWriter(new FileWriter(log, true));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			int choice = Integer.parseInt(in.readLine());

			if (choice == 1) {// PULL
				File file = new File(in.readLine()); // Odbiera sciezke
				if (file.exists()) {
					out.println("1;" + file.length()); // Wysyla, ¿e plik
														// istnieje
					String mesg = in.readLine();

					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);
					OutputStream os = socket.getOutputStream();
					byte[] cont;
					long cur = 0;
					if (mesg.split(";")[0].equals("exists")) {
						long ll = Long.parseLong(mesg.split(";")[1]);
						cur = ll;
						fis.getChannel().position(ll);
					}

					long fileLength;
					if (Integer.parseInt(mesg.split(";")[2]) == 0) {
						fileLength = file.length();
					} else {
						fileLength = Integer.parseInt(mesg.split(";")[2]);
					}
					long start = System.nanoTime();
					while (cur != fileLength) {
						int size = 10000;
						if (fileLength - cur >= size) {
							cur += size;
						} else {
							size = (int) (fileLength - cur);
							cur = fileLength;
						}
						cont = new byte[size];

						bis.read(cont, 0, size);

						os.write(cont);
						System.out.print("Wysy³anie pliku... " + (cur * 100) / fileLength + "% complete!*\r");
						pw.println(new Date() + ": Wys³anie pliku ...<br>");
					}

					os.flush();
					socket.close();
					
					bis.close();
					fis.close();

					System.out.println("Plik wys³any poprawnie!");
					pw.println(new Date() + ": Plik wys³any poprawnie <br>");
				} else {
					System.out.println("Nie ma takiego pliku");
					pw.println(new Date() + ": Nie ma takiego pliku <br>");
					out.println(0);
				}

			} else if (choice == 2) { // PUSH

				if (in.readLine().equals("1")) { // Plik istnieje
					byte[] contents = new byte[10000];
					String name = in.readLine(); // sciezka do pliku
					File filePart = new File(name + ".part");

					if (filePart.exists()) {
						out.println("exists;" + filePart.length());
					} else
						out.println("0;0");

					FileOutputStream fos = new FileOutputStream(name + ".part", true);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					InputStream is = socket.getInputStream();

					int bytesRead = 0;
					System.out.println("Odbieranie pliku...");
					pw.println(new Date() + ": Odbieranie pliku... <br>");
					while ((bytesRead = is.read(contents)) != -1) {
						bos.write(contents, 0, bytesRead);
					}
					bos.flush();
					socket.close();
					fos.close();
					bos.close();
					Thread.sleep(1000);
					System.out.println("Plik zapisany poprawnie!");
					pw.println(new Date() + ": Plik zapisany poprawnie! <br>");
					File oldfile = new File(name + ".part");
					File newfile = new File(name);

					if (oldfile.renameTo(newfile)) {
						System.out.println("Nazwa pliku zmieniona.");
					} else {
						System.out.println("Nie mo¿na zmieniæ nazwy pliku");
					}
					update();
				}

			}

		} catch (Exception e1) {
		}

		try {
			socket.close();
		} catch (IOException e) {
		}
	}
}
