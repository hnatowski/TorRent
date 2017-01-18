import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class ServerThread extends Thread {
	public static int ids = 0;
	private Socket socket;
	public static Vector<CL> CList = new Vector<CL>();
	BufferedReader in;
	PrintWriter out;

	public ServerThread(Socket socket) throws IOException {
		super();
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

	}

	public void run() {
		try {

			String st = in.readLine();
			if (st.equals("999")) {
				ids++;
				out.println(ids);
			}
			String n = in.readLine();
			if (n != null) {

				if (n.substring(0, 5).equals("start")) {
					String name = n.substring(5);
					CList.add(new CL(name));
					Collections.sort(CList, new Comparator<CL>() {
						public int compare(CL f1, CL f2) {
							Integer i= Integer.parseInt(f1.toString().split(";")[0]);
							Integer j= Integer.parseInt(f2.toString().split(";")[0]);
							 return i.compareTo(j);
						}
					});

				} else if (n.substring(0, 5).equals("lista")) {
					out.println(CList.size());
					for (int i = 0; i < CList.size(); i++) {
						out.println(CList.elementAt(i));
					}
				} else if (n.substring(0, 5).equals("PULL.")) {
					String[] odb = in.readLine().split(";");
					String a = odb[1];
					if(Integer.parseInt(a)==999){
						String b=odb[2];
						int pom=0;
						for(CL cli:CList){
							if(cli.toString().contains(b+";"))
							pom++;
						}
						out.println(pom);
						for(CL cli:CList){
							if(cli.toString().contains(b+";"))
							out.println(cli.toString());
						}
					}
					else if (Integer.parseInt(a) > ids || Integer.parseInt(a) < 1)
						out.println(-1);
					else
						out.println(CList.elementAt(Integer.parseInt(a) - 1));
				}else if (n.substring(0, 5).equals("Updat")) {
				String pom = in.readLine();
				System.out.println(pom);
				System.out.println(pom.split(";")[0]);
					CList.set(Integer.parseInt(pom.split(";")[0])-1, new CL(pom));
					
				}
			}
		} catch (IOException e1) {
		}

		try {
			socket.close();
		} catch (IOException e) {
		}
	}

}