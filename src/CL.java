import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

public class CL {
	static int id=0;
	String s;
	Vector<String> names = new Vector();
	Vector<String> MD5 = new Vector();
	int port;
	InetAddress ip;
	public CL(String s) throws UnknownHostException{
		this.ip=ip;
		this.port=port;
		this.s=s;
		StringTokenizer st = new StringTokenizer(s, ";");
		this.id=Integer.parseInt(st.nextToken());
		while(st.hasMoreTokens()){
			names.addElement(st.nextToken());
			MD5.addElement(st.nextToken());
		}
		this.ip=InetAddress.getByName(names.lastElement());
		this.port=Integer.parseInt(MD5.lastElement());
		names.removeElementAt(names.size()-1);
		MD5.removeElementAt(MD5.size()-1);
	}
	
	public String toString(){
		return s;
	}
}
