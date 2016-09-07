package com.zhy3.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {
	
	private Selector selector;
	
	private ServerSocketChannel serverSocketChannel;
	
	private volatile boolean stop;

	//初始化多路复用器，绑定监听端口
	public MultiplexerTimeServer(int port) {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(new InetSocketAddress(port), 1024);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("The time server is start in port : " + port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
				
	}
	
	public void stop() {
		stop = true;
	}
	
	@Override
	public void run() {
		while(!stop) {
			try {
				selector.select(1000);//估计意思是休眠时间1S
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();
				SelectionKey key = null;
				while(iter.hasNext()) {
					key = iter.next();
					iter.remove();
					try {
						this.handleInput(key);
					} catch (Exception e) {
						if (key != null) {
							key.cancel();
							if (key.channel() != null) {
								key.channel().close();
							}
						}
						e.printStackTrace();
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			//多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
		}
	}
	
	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			//处理新接入的请求消息
			if (key.isAcceptable()) {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
			}
			if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);//开辟一个1MB的缓冲区
				int readBytes = sc.read(readBuffer);
				/*
				 * readBytes > 0      读到了字节，对字节进行编解码
				 * readBytes = 0      没有读取到字节，属于正常场景，忽略 
				 * readBytes = -1   链路已经关闭，需要关闭SocketChannel,释放资源
				 */
				if (readBytes > 0) {
					readBuffer.flip();//将缓冲区当前的limit设置为position，position设置为0，用于后续对缓冲区的读取操作。
					byte[] bytes = new byte[readBuffer.remaining()];//根据缓冲区可读的字节个数创建字节数组
					readBuffer.get(bytes);//将缓冲区可读的字节数组复制到新创建的字节数组中
					String body = new String(bytes, "UTF-8");
					System.out.println("The time server receive order :" + body);
					String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
					doWrite(sc, currentTime);
					
				}
			}
		} 
	}

	private void doWrite(SocketChannel channel, String response) throws IOException {
		if (response != null || response.trim().length() > 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);
		}
	}
	
	
}
