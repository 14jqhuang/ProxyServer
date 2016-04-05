import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Blacklist {
	/**
	 * Returns a block message to the client for trying to access
	 * a blocked website and handles the socket connections
	 * @param url url to be checked for being on the blacklist
	 * @param clientSocket socket accepted from the client
	 */
	public static void blockWebsite(String url, Socket clientSocket) {
		try {
			System.out.println(url + " is a blacklisted website!");
			String message = "<html>"
					+ "<head>"
					+ "<title>Blocked!</title>"
					+ "<meta charset=\"utf-8\">"
					+ "</head>"
					+

					"<body>"
					+ "<h1><font face=\"verdana\">WARNING!</font></h1>"
					+ "<p><font face=\"verdana\" color=\"red\" size=\"4\">" + url + "</font>"
					+ "<font face=\"verdana\" size=\"4\"> has been blacklisted from use.</font></p>"
					+ "<p><font face=\"verdana\" size=\"4\">Please contact your system administrator.</font></p>"
					+ "</body>" 
					+ "</html>\r\n";

			byte[] message_b = message.getBytes();
			final OutputStream to_c = clientSocket.getOutputStream();
			final InputStream stream = new ByteArrayInputStream(
					message.getBytes(StandardCharsets.UTF_8));

			int bytesRead2;
			try {
				while ((bytesRead2 = stream.read(message_b)) != -1) {
					to_c.write(message_b, 0, bytesRead2);
					to_c.flush();
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks the url provided if it exists on the blacklist
	 * @param url url to be checked
	 * @return boolean true or false for if the url is on the list
	 */
	public static boolean checkList(String url) {
		try {
	        String currentLine = "";
			File file = new File("./admin/Blacklist.txt");
			Scanner scanner = new Scanner(file);
			while(scanner.hasNext()) {
				currentLine = scanner.next();
				if(currentLine.contains(url)) {
					System.out.println(url + " is a blacklisted website!");
					scanner.close();
					return true;
				}
			}
			scanner.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println(url + " is a whitelisted website!");
		return false;
	}

}
