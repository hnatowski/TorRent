import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.bind.DatatypeConverter;

public class User extends Thread {
	int id;
	Socket socket;
	BufferedReader in;
	PrintWriter out;
	ServerSocket server;
	File log;
	PrintWriter pw;

	public User(int id, ServerSocket server) throws IOException {
		this.id = id;
		this.server = server;
		System.out.println("-----------------------------" + id + "-----------------------------");
		File log = new File(Config.Dir + "_" + id + "\\" + "log.txt");
		pw = new PrintWriter(new FileWriter(log));
		pw.println(new Date() + ": Uruchomienie klienta nr. " + id + "<br>");
		pw.close();
	}

	@Override
	public void run() {
		try {

			while (true) {
				File log = new File(Config.Dir + "_" + id + "\\" + "log.txt");
				pw = new PrintWriter(new FileWriter(log, true));
				socket = new Socket(Config.adress, Config.SPort);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				Scanner sc = new Scanner(System.in);
				int choice;
				do {
					System.out.println("1. Poka¿ listy plików hostów.");
					System.out.println("2. Œci¹gnij plik (PULL).");
					System.out.println("3. Wrzuæ plik (PUSH).");
					System.out.println("4. Œci¹gnij plik (Multihost).");
					while (!sc.hasNextInt()) {
						System.out.println("Podaj wlasciwa opcje.");
						sc.next();
					}
					choice = sc.nextInt();
				} while (choice < 0 && choice > 4); // Tu zmieniæ, jesli dojda
													// jakies opcje do menu

				if (choice == 1) {
					pw.println(new Date() + ": Wys³ano proœbê o listê plików <br>");
					out.println(1);
					out.println("lista");
					int ile = Integer.parseInt(in.readLine());
					pw.println(new Date() + ": Otrzymano listê plików: <br>");
					for (int i = 0; i < ile; i++) {
						String c = in.readLine();
						System.out.println(view(c));
						pw.println(view1(c) + "<br>");
					}

				} else if (choice == 2) {

					int ch;

					do {
						System.out.println("Od którego hosta pobraæ plik? (Podaj numer)");

						while (!sc.hasNextInt()) {
							System.out.println("Podaj liczbe.");
							sc.next();
						}
						ch = sc.nextInt();
					} while (choice < 0 && choice > 3); // Tu zmieniæ, jesli
														// dojda
														// jakies opcje do menu

					System.out.println("Który plik? (Podaj nazwê)");
					String ch1 = sc.next();
					pw.println();
					out.println(1);
					out.println("PULL.");
					out.println("PULL" + ";" + ch + ";" + ch1);
					pw.println(new Date() + ": Chcê pobraæ plik: " + ch1 + " od hosta nr. " + ch + " <br>");
					String mesg = in.readLine();
					if (mesg.equals("-1")) {
						System.out.println("Nie ma takiego hosta");
						pw.println(new Date() + ": Nie ma takiego hosta.<br>");
					} else {
						CL cl = new CL(mesg);
						socket = new Socket(cl.ip, cl.port);
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						out = new PrintWriter(socket.getOutputStream(), true);

						out.println(1); // PULL
						File filePart = new File(Config.Dir + "_" + id + "\\" + ch1 + ".part");
						File file = new File(Config.Dir + "_" + cl.id + "\\" + ch1);
						out.println(file.getAbsolutePath()); // Wysyla
																// sciezke
						if (in.readLine().split(";")[0].equals("1")) { // Dostaje
																		// informacje,
							// ¿e plik istnieje
							if (filePart.exists()) {
								out.println("exists;" + filePart.length() + ";" + 0);
							} else
								out.println("0;0;0");

							byte[] contents = new byte[10000];

							FileOutputStream fos = new FileOutputStream(Config.Dir + "_" + id + "\\" + ch1 + ".part",
									true);
							BufferedOutputStream bos = new BufferedOutputStream(fos);
							InputStream is = socket.getInputStream();

							int bytesRead = 0;
							System.out.println("Obieram plik...");
							pw.println(new Date() + ": Odbieram plik...<br>");
							while ((bytesRead = is.read(contents)) != -1) {
								bos.write(contents, 0, bytesRead);
							}

							bos.flush();
							socket.close();
							fos.close();
							bos.close();
							Thread.sleep(1000);
							System.out.println("Plik zapisany.!");
							pw.println(new Date() + ": Plik " + ch1 + " zapisany<br>");

							File oldfile = new File(Config.Dir + "_" + id + "\\" + ch1 + ".part");
							File newfile = new File(Config.Dir + "_" + id + "\\" + ch1);

							if (oldfile.renameTo(newfile)) {
								System.out.println("Nazwa pliku zmieniona.");
							} else {
								System.out.println("Nie mo¿na zmieniæ nazwy pliku.");
							}

							update();
						} else {
							System.out.println("Nie ma takiego pliku.");
							pw.println(new Date() + ": Nie ma takiego pliku.<br>");
						}

					}
				} else if (choice == 3) {

					int ch;

					do {
						System.out.println("Na którego hosta chcesz wrzuciæ plik? (Podaj numer)");

						while (!sc.hasNextInt()) {
							System.out.println("Podaj liczbe.");
							sc.next();
						}
						ch = sc.nextInt();
					} while (choice < 0 && choice > 4); // Tu zmieniæ, jesli
														// dojda
														// jakies opcje do menu

					System.out.println("Który plik? (Podaj nazwê)");
					String ch1 = sc.next();
					pw.println(new Date() + ": Chcê wrzuciæ plik: " + ch1 + " na hosta nr. " + ch + " <br>");
					out.println(1);
					out.println("PULL.");
					out.println("PULL" + ";" + ch + ";" + ch1);
					String mesg = in.readLine();
					if (mesg.equals("-1")) {
						System.out.println("Nie ma takiego hosta");
						pw.println(new Date() + ": Nie ma takiego hosta. <br>");
					} else {
						CL cl = new CL(mesg);
						socket = new Socket(cl.ip, cl.port);
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						out = new PrintWriter(socket.getOutputStream(), true);

						out.println(2); // PUSH
						File file = new File(Config.Dir + "_" + id + "\\" + ch1);
						// System.out.println(file + " z tego idzie");
						if (file.exists()) {
							out.println(1); // Wysyla, ¿e plik istnieje
							out.println(Config.Dir + "_" + ch + "\\" + ch1); // Wysyla
																				// sciezke
																				// gdzie
																				// ma
																				// byc
																				// zapisany
																				// plik

							String mesgg = in.readLine();

							FileInputStream fis = new FileInputStream(file);
							BufferedInputStream bis = new BufferedInputStream(fis);
							OutputStream os = socket.getOutputStream();
							byte[] cont;

							long cur = 0;
							if (mesgg.split(";")[0].equals("exists")) {
								long ll = Long.parseLong(mesgg.split(";")[1]);
								cur = ll;
								fis.getChannel().position(ll);
							}

							long fileLength = file.length();
							long start = System.nanoTime();
							int pom = 0;
							while (cur != fileLength) {
								int size = 10000;
								if (fileLength - cur >= size)
									cur += size;
								else {
									size = (int) (fileLength - cur);
									cur = fileLength;
								}

								cont = new byte[size];

								bis.read(cont, 0, size);
								if (pom < 10) {
									Thread.sleep(10);
									pom++;
								}
								System.out.print("Wysy³anie pliku ... " + (cur * 100) / fileLength + "% complete!\r");
								pw.println(new Date() + ": Wysy³anie pliku ...<br>");
								os.write(cont);

							}

							os.flush();
							socket.close();
							bis.close();
							fis.close();

							System.out.println("Plik wys³any poprawnie!");
							pw.println(new Date() + ": Plik wys³any poprawnie. <br>");

						} else {
							System.out.println("Nie ma takiego pliku");
							pw.println(new Date() + ": Nie ma takiego pliku. <br>");
							out.println(0);
						}
					}
				}

				else if (choice == 4) {

					System.out.println("Który plik? (Podaj nazwê)");
					String ch1 = sc.next();
					pw.println();
					out.println(1);
					out.println("PULL.");
					out.println("PULL" + ";" + 999 + ";" + ch1);
					pw.println(new Date() + ": Chcê pobraæ plik: " + ch1 + " od wszystkich mo¿liwych hostów <br>");
					int pom = Integer.parseInt(in.readLine());

					if (pom == 0) {
						System.out.println("Na ¿adnym hoœcie nie znaleziono pliku " + ch1 + ".");
						pw.println(new Date() + ": Na ¿adnym hoœcie nie znaleziono pliku " + ch1 + ".<br>");
					} else {
						CL[] cl = new CL[pom];
						for (int i = 0; i < pom; i++) {
							cl[i] = new CL(in.readLine());
						}
						// ---------------------------------------------------------------
						Thread[] watki = new Thread[pom];
						for (int j = 0; j < pom; j++) {

							int i = j;
							watki[i] = new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										Socket socket = new Socket(cl[i].ip, cl[i].port);
										BufferedReader in = new BufferedReader(
												new InputStreamReader(socket.getInputStream()));
										PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

										out.println(1); // PULL
										File filePart = new File(Config.Dir + "_" + id + "\\" + ch1 + ".part" + i);
										File file = new File(Config.Dir + "_" + cl[i].id + "\\" + ch1);
										out.println(file.getAbsolutePath()); // Wysyla
										// sciezke
										String[] exis = in.readLine().split(";");
										if (exis[0].equals("1")) { // Dostaje
											// ¿e plik istnieje

											int dlugosc = (int) (Integer.parseInt(exis[1]) / pom);
											int koniec;
											if (i != pom - 1)
												koniec = dlugosc * (i + 1);
											else
												koniec = Integer.parseInt(exis[1]);
											out.println("exists;" + dlugosc * i + ";" + koniec);

											byte[] contents = new byte[10000];

											FileOutputStream fos = new FileOutputStream(
													Config.Dir + "_" + id + "\\" + ch1 + ".part" + i, true);
											BufferedOutputStream bos = new BufferedOutputStream(fos);
											InputStream is = socket.getInputStream();

											int bytesRead = 0;
											System.out.println("Obieram plik...");
											pw.println(new Date() + ": Odbieram plik...<br>");
											while ((bytesRead = is.read(contents)) != -1) {
												bos.write(contents, 0, bytesRead);
											}

											bos.flush();
											socket.close();
											fos.close();
											bos.close();
											Thread.sleep(1000);
											System.out.println("Part " + i + " zapisany");
											pw.println(new Date() + ": Part " + i + " zapisany<br>");

										} else {
											System.out.println("Nie ma takiego pliku.");
											pw.println(new Date() + ": Nie ma takiego pliku.<br>");
										}

									} catch (Exception e) {

									}
								}
							});

						}

					
						
						for (int i = 0; i < pom; i++) {
							watki[i].start();
						}

						for (int i = 0; i < pom; i++) {
							watki[i].join();
						}

						System.out.println("Wszystkie czesci odebrane. Scalam plik...");
						pw.println(new Date() + ": Wszystkie czesci odebrane. Scalam plik...<br>");
						// File oldfile = new File(Config.Dir + "_" + id + "\\"
						// + ch1 + ".part");
						// File newfile = new File(Config.Dir + "_" + id + "\\"
						// + ch1);
						//
						// if (oldfile.renameTo(newfile)) {
						// System.out.println("Nazwa pliku zmieniona.");
						// } else {
						// System.out.println("Nie mo¿na zmieniæ nazwy pliku.");
						// }

						File[] listaPartow = new File[pom];
						for (int i = 0; i < pom; i++) {
							listaPartow[i] = new File(Config.Dir + "_" + id + "\\" + ch1 + ".part" + i);
						}
						File ofile = new File(Config.Dir + "_" + id + "\\" + ch1);

						FileOutputStream fos = null;
						FileInputStream fis;
						byte[] fileBytes;
						int bytesRead = 0;
						try {
							fos = new FileOutputStream(ofile, true);
							for (File file : listaPartow) {
								fis = new FileInputStream(file);
								fileBytes = new byte[(int) file.length()];
								bytesRead = fis.read(fileBytes, 0, (int) file.length());
								assert (bytesRead == fileBytes.length);
								assert (bytesRead == (int) file.length());
								fos.write(fileBytes);
								fos.flush();
								fileBytes = null;
								fis.close();
								fis = null;
							}
						} catch (Exception e) {

						}
						fos.close();
						fos = null;
						for (int i = 0; i < pom; i++) {
							listaPartow[i].delete();
						}
						System.out.println("Plik scalony.");
						pw.println(new Date() + ": Plik scalony.<br>");
					}
					// -----------------------------------------------------------------------------------
				
				
				}
				
				
				update();
				socket.close();
				pw.flush();
				pw.close();
			}

		} catch (

		IOException e) {

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		pw.println(new Date() + ": Zaaktualizowano listê plików. <br>");
	}

	public static String view(String a) {
		StringTokenizer st = new StringTokenizer(a, ";");
		StringBuffer sb = new StringBuffer();
		Vector<String> dane = new Vector();
		while (st.hasMoreTokens()) {
			dane.addElement(st.nextToken());
		}
		sb.append("-----------------------------" + dane.elementAt(0) + "-----------------------------\n\r");
		for (int i = 1; i < dane.size() - 2; i += 2) {
			sb.append(dane.elementAt(i) + " " + dane.elementAt(i + 1) + "\n\r");

		}
		sb.append("-----------------------------------------------------------\n\r");
		return sb.toString();
	}

	public static String view1(String a) {
		StringTokenizer st = new StringTokenizer(a, ";");
		StringBuffer sb = new StringBuffer();
		Vector<String> dane = new Vector();
		while (st.hasMoreTokens()) {
			dane.addElement(st.nextToken());
		}
		sb.append("-----------------------------" + dane.elementAt(0) + "-----------------------------\n\r <br>");
		for (int i = 1; i < dane.size() - 2; i += 2) {
			sb.append(dane.elementAt(i) + " " + dane.elementAt(i + 1) + "\n\r<br>");

		}
		sb.append("-----------------------------------------------------------\n\b<br>");
		return sb.toString();
	}
}
