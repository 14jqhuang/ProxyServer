package com.tomyca.proxyserver;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy extends Thread {
	
	public Proxy() {
		super("Proxy Server");
	}

	public static void main(String[] args) {
		(new Proxy()).run();
	}
	
	/**
	 * Spawns threads for connection and superuser handling
	 */
	@Override
	public void run() {
		
		try(ServerSocket proxySocket = new ServerSocket(Helpers.PORT)) {
			Socket socket;
			System.out.println("Proxy server started on " + Helpers.HOST + ":" + Helpers.PORT);
			try {
				while((socket = proxySocket.accept()) != null) {
					new Pooling(socket).start();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
