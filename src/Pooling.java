import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;

import com.sun.jndi.toolkit.url.Uri;

public class Pooling extends Thread {
        private Socket clientSocket;
        private boolean previousWasR = false;
        
        private Matcher matcherConnect;
        private Matcher matcherGet;

        public Pooling(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                String request = readLine(clientSocket);
                System.out.println(request);
                matcherConnect = Helpers.CONNECT_PATTERN.matcher(request);
                matcherGet = Helpers.GET_PATTERN.matcher(request);
                
                if (matcherConnect.matches()) {
                	SSL();
                } else if(matcherGet.matches()) {
                	System.out.println(matcherGet.group(1));
                	if(matcherGet.group(1).equals("www.glassbyte.com")){
                		System.out.println(matcherGet.group(1) + " has been blacklisted!");
                		Blacklist.block(matcherGet.group(1), clientSocket);	
                	} else {
                		System.out.println(matcherGet.group(1) + " has been whitelisted!");
                		HTTP();
                	}
                }
            } catch (IOException e) {
                e.printStackTrace();  
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace(); 
                }
            }
        }
        
        private void HTTP() throws IOException {
            
            Uri uri = new Uri("http://" + matcherGet.group(1) + "/");
            URL url = new URL("http://" + matcherGet.group(1) + "/");

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", Helpers.USER_AGENT);
            
            int responseCode = connection.getResponseCode();
    		System.out.println("\nSending 'GET' request to URL : " + url);
    		System.out.println("Response Code : " + responseCode);

    		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    		String inputLine;
    		StringBuffer response = new StringBuffer();

    		while ((inputLine = in.readLine()) != null) {
    			response.append(inputLine);
    		}
    		in.close();

    		//print result
    		System.out.println(response.toString());
    		byte[] buffer = response.toString().getBytes();

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(buffer);
            outputStream.flush();
            outputStream.close();
            clientSocket.close();
        }
        
        private void SSL() throws IOException {

            String header;
            do {
                header = readLine(clientSocket);
                System.out.println(header);
            } while (!"".equals(header));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream(),
                                                                           "ISO-8859-1");

            final Socket forwardSocket;
            try {
                forwardSocket = new Socket(matcherConnect.group(1), Integer.parseInt(matcherConnect.group(2)));
                System.out.println(forwardSocket);
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();  // TODO: implement catch
                outputStreamWriter.write("HTTP/" + matcherConnect.group(3) + " 502 Bad Gateway\r\n");
                outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                outputStreamWriter.write("\r\n");
                outputStreamWriter.flush();
                return;
            }
            try {
                outputStreamWriter.write("HTTP/" + matcherConnect.group(3) + " 200 Connection established\r\n");
                outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                outputStreamWriter.write("\r\n");
                outputStreamWriter.flush();

                Thread remoteToClient = new Thread() {
                    @Override
                    public void run() {
                        forwardData(forwardSocket, clientSocket);
                    }
                };
                remoteToClient.start();
                try {
                    if (previousWasR) {
                        int read = clientSocket.getInputStream().read();
                        if (read != -1) {
                            if (read != '\n') {
                                forwardSocket.getOutputStream().write(read);
                            }
                            forwardData(clientSocket, forwardSocket);
                        } else {
                            if (!forwardSocket.isOutputShutdown()) {
                                forwardSocket.shutdownOutput();
                            }
                            if (!clientSocket.isInputShutdown()) {
                                clientSocket.shutdownInput();
                            }
                        }
                    } else {
                        forwardData(clientSocket, forwardSocket);
                    }
                } finally {
                    try {
                        remoteToClient.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace(); 
                    }
                }
            } finally {
                forwardSocket.close();
            }
        }

        private static void forwardData(Socket inputSocket, Socket outputSocket) {
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
                e.printStackTrace();  
            }
        }

        private String readLine(Socket socket) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int next;
            readerLoop:
            while ((next = socket.getInputStream().read()) != -1) {
                if (previousWasR && next == '\n') {
                    previousWasR = false;
                    continue;
                }
                previousWasR = false;
                switch (next) {
                    case '\r':
                        previousWasR = true;
                        break readerLoop;
                    case '\n':
                        break readerLoop;
                    default:
                        byteArrayOutputStream.write(next);
                        break;
                }
            }
            return byteArrayOutputStream.toString("ISO-8859-1");
        }
    }