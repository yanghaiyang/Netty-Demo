package com.zhy3.client;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TimeClientHandler implements Runnable {
	
	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;
	
	public TimeClientHandler(int port, String host) {
		this.port = port;
		this.host = host == null ? "127.0.0.1" : host;
		selector = selector.open();
		
	}
	
	
	
}
