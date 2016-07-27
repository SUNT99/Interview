

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/*
 * 主要思路：Http Server监听8080端口，获取客户端的请求
 * 对请求的url进行简单的解析，获取/answer/?l= 后面的数字 存入数组中
 * 计算所有数字相乘之积sum
 * 将sum除以对应位置的值即为每一个位置的返回值
 * 
 */
public class SimpleHttpServer {
	private int port = 8080;  //监听的端口
	private ServerSocketChannel serverSocketChannel = null;
	private ExecutorService executorService;
	private static final int POOL_MULTIPLE = 4;//线程池一次可以访问四个客户端请求

	public SimpleHttpServer() throws IOException {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_MULTIPLE);
		serverSocketChannel = serverSocketChannel.open();//开启通道
		serverSocketChannel.socket().setReuseAddress(true);//设置选项
		serverSocketChannel.socket().bind(new InetSocketAddress(port));//绑定端口
		System.out.println("服务器启动了");
	}

	//接受请求
	public void service() {
		while (true) {
			SocketChannel socketChannel = null;
			try {
				socketChannel = serverSocketChannel.accept();
				executorService.execute(new Handler(socketChannel));//分配一个去处理
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new SimpleHttpServer().service();
	}

	//核心处理类
	class Handler implements Runnable {
		private SocketChannel socketChannel;

		public Handler(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
		}

		public void run() {
			try {
				handle(socketChannel);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        //处理客户端请求方法
		public void handle(SocketChannel socketChannel) throws IOException {
			Socket socket = socketChannel.socket();
			System.out.println("收到客户端连接,来自" + socket.getInetAddress() + ":" + socket.getPort());
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			socketChannel.read(buffer);// 接收HTTP请求，假定其长度不超过1024
		
		//	String result = request.substring(request.indexOf("/answer/?l=", request.indexOf("\r\n")));
			String text=socket.getInetAddress().toString();
       	//	String text="http://duitang/answer/?l=1,7,3,4\r\n";测试例子
			String result = text.substring(text.indexOf("/answer/?l=")+11,text.indexOf("\r\n"));
			int[] ar = new int[result.length()];
			StringTokenizer st = new StringTokenizer(result, ",");
			int sum = 1, i = 0, elem;
			while (st.hasMoreTokens()) {
				elem = Integer.parseInt(st.nextToken());
				sum *= elem;//求得所有数的乘积
				ar[i] = elem;
				i++;
			}
			int value;
			StringBuffer finalresult = new StringBuffer();
			for (int j = 0; j < i; j++) {

				if (ar[j] != 0 && j != i - 1) {
					value = sum / ar[j];//sum/每一个位置的值作为返回值
					finalresult.append(Integer.toString(value) + ",");
				}
			}
			finalresult.append(Integer.toString(sum / ar[i - 1]));
       //     System.out.println(finalresult);
			
		}
	}
}