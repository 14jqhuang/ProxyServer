import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Matcher;

public class Pooling extends Thread {
        private final Socket clientSocket;
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
                	HTTP();
                }
            } catch (IOException e) {
                e.printStackTrace();  // TODO: implement catch
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();  // TODO: implement catch
                }
            }
        }
        
        private void HTTP() throws IOException {
        	
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
                        e.printStackTrace();  // TODO: implement catch
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
                e.printStackTrace();  // TODO: implement catch
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