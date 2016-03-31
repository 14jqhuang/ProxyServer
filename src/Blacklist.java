import java.io.OutputStreamWriter;
import java.net.Socket;

public class Blacklist {
	
	public Blacklist() { }
	
	public static void block(String url, Socket socket) {
		String message =
			"<html>" + 
				"<head>" +
					"<title>Blocked!</title>" +
					"<meta charset=\"utf-8\">" + 
				"</head>"+
	
				"<body>" +
					"<h1><font face=\"verdana\">WARNING!</font></h1>" +
					"<p><font face=\"verdana\" color=\"red\" size=\"4\">" + 
					url +
					"</font><font face=\"verdana\" size=\"4\"> has been blacklisted from use.</font> </p>"+
					"<p><font face=\"verdana\" size=\"4\">Please contact your system administrator.</font></p>" +
					"<p><font face=\"verdana\" size=\"4\">No wanking ;p</font></p>" +
				"</body>"+
			"</html>";
		
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1");			
			outputStreamWriter.write("HTTP/1.1 200 OK Connection Established\r\n");
            outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
			outputStreamWriter.write("Content-Type: text/html\r\n");
			outputStreamWriter.write("Content-Size: " + message.length() + "\r\n");
			outputStreamWriter.write("Connection: close\r\n");
			outputStreamWriter.write("\r\n");
			outputStreamWriter.write(message);
			outputStreamWriter.flush();
			outputStreamWriter.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private static boolean checkList(String url){
		return false;
	}

}
