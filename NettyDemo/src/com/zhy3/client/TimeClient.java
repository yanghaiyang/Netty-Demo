package com.zhy3.client;

import java.io.IOException;


public class TimeClient {

	public static void main(String[] args) throws IOException {
		int port = 8080;
		if (args != null && args.length > 0) {
			port = Integer.valueOf(args[0]);
		}
		new Thread(new TimeClientHandler(8080, "127.0.0.1")).start();
	}
}
