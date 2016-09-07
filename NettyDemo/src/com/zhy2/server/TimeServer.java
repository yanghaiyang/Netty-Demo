package com.zhy2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class TimeServer {

	public static void main(String[] args) throws IOException {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("The time server is start in port : " + port);
			Socket socket = null;
			TimeServerHandlerExecutePool singleExecutor = new TimeServerHandlerExecutePool(50,10000);//创建I/O任务线程池
			while (true) {
				socket = server.accept();
				singleExecutor.execute(new TimeServerHandler(socket));
			}
		} finally {
			if (server != null) {
				System.out.println("The time server colse");
				server.close();
				server = null;
			}
		}
	}
	
	
}
