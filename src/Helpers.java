import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {
	public static final int PORT = 2037;
	public static final String HOST = "localhost";
	public static final String USER_AGENT = "Mozilla/5.0";
	
    public static final Pattern GET_PATTERN = 
    		Pattern.compile("GET http://(.*)/ HTTP/(1\\.[01])",
            Pattern.CASE_INSENSITIVE);
	
	public static final Pattern CONNECT_PATTERN = 
			Pattern.compile("CONNECT (.+):(.+) HTTP/(1\\.[01])",
			Pattern.CASE_INSENSITIVE);
	
	public static void logWebsite(String url) {
		
	}
	
	public static String determineReq(String request) {
		Matcher matcherGet = Helpers.GET_PATTERN.matcher(request);
		Matcher matcherConnect = Helpers.CONNECT_PATTERN.matcher(request);
		
		if(matcherGet.matches()) {
			return "HTTP";
		} else if(matcherConnect.matches()) {
			return "SSL";
		} else {
			return null;
		}
	}
	
	public static String getRemoteAddress(String request) {

		Matcher matcherGet = Helpers.GET_PATTERN.matcher(request);
		Matcher matcherConnect = Helpers.CONNECT_PATTERN.matcher(request);
		
		if(matcherGet.matches()) {
			return matcherGet.group(1);
		} else if(matcherConnect.matches()) {
			return matcherConnect.group(1);
		} else {
			return null;
		}
	}
	
	public static int getRemotePort(String request) {

		Matcher matcherGet = Helpers.GET_PATTERN.matcher(request);
		Matcher matcherConnect = Helpers.CONNECT_PATTERN.matcher(request);
		
		if(matcherGet.matches()) {
			return Integer.parseInt(matcherGet.group(2));
		} else if(matcherConnect.matches()) {
			return Integer.parseInt(matcherConnect.group(2));
		} else {
			return -1;
		}
	}
	
	public static String getHTTPVersion(String request) {

		Matcher matcherGet = Helpers.GET_PATTERN.matcher(request);
		Matcher matcherConnect = Helpers.CONNECT_PATTERN.matcher(request);
		
		if(matcherGet.matches()) {
			return matcherGet.group(3);
		} else if(matcherConnect.matches()) {
			return matcherConnect.group(3);
		} else {
			return null;
		}
	}
	
	public static void forwardData(Socket inputSocket, Socket outputSocket) {
        try {
            InputStream inputStream = inputSocket.getInputStream();
            try {
                OutputStream outputStream = outputSocket.getOutputStream();
                try {
                    byte[] buffer = new byte[4096];
                    int read;
                    do {
                        read = inputStream.read(buffer);
                        if (read > 0) {
                            outputStream.write(buffer, 0, read);
                            if (inputStream.available() < 1) {
                                outputStream.flush();
                            }
                        }
                    } while (read >= 0);
                } finally {
                    if (!outputSocket.isOutputShutdown()) {
                        outputSocket.shutdownOutput();
                    }
                }
            } finally {
                if (!inputSocket.isInputShutdown()) {
                    inputSocket.shutdownInput();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  // TODO: implement catch
        }
    }
}
