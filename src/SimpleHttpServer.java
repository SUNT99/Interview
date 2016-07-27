

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
 * ��Ҫ˼·��Http Server����8080�˿ڣ���ȡ�ͻ��˵�����
 * �������url���м򵥵Ľ�������ȡ/answer/?l= ��������� ����������
 * ���������������֮��sum
 * ��sum���Զ�Ӧλ�õ�ֵ��Ϊÿһ��λ�õķ���ֵ
 * 
 */
public class SimpleHttpServer {
	private int port = 8080;  //�����Ķ˿�
	private ServerSocketChannel serverSocketChannel = null;
	private ExecutorService executorService;
	private static final int POOL_MULTIPLE = 4;//�̳߳�һ�ο��Է����ĸ��ͻ�������

	public SimpleHttpServer() throws IOException {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_MULTIPLE);
		serverSocketChannel = serverSocketChannel.open();//����ͨ��
		serverSocketChannel.socket().setReuseAddress(true);//����ѡ��
		serverSocketChannel.socket().bind(new InetSocketAddress(port));//�󶨶˿�
		System.out.println("������������");
	}

	//��������
	public void service() {
		while (true) {
			SocketChannel socketChannel = null;
			try {
				socketChannel = serverSocketChannel.accept();
				executorService.execute(new Handler(socketChannel));//����һ��ȥ����
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new SimpleHttpServer().service();
	}

	//���Ĵ�����
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
        //����ͻ������󷽷�
		public void handle(SocketChannel socketChannel) throws IOException {
			Socket socket = socketChannel.socket();
			System.out.println("�յ��ͻ�������,����" + socket.getInetAddress() + ":" + socket.getPort());
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			socketChannel.read(buffer);// ����HTTP���󣬼ٶ��䳤�Ȳ�����1024
		
		//	String result = request.substring(request.indexOf("/answer/?l=", request.indexOf("\r\n")));
			String text=socket.getInetAddress().toString();
       	//	String text="http://duitang/answer/?l=1,7,3,4\r\n";��������
			String result = text.substring(text.indexOf("/answer/?l=")+11,text.indexOf("\r\n"));
			int[] ar = new int[result.length()];
			StringTokenizer st = new StringTokenizer(result, ",");
			int sum = 1, i = 0, elem;
			while (st.hasMoreTokens()) {
				elem = Integer.parseInt(st.nextToken());
				sum *= elem;//����������ĳ˻�
				ar[i] = elem;
				i++;
			}
			int value;
			StringBuffer finalresult = new StringBuffer();
			for (int j = 0; j < i; j++) {

				if (ar[j] != 0 && j != i - 1) {
					value = sum / ar[j];//sum/ÿһ��λ�õ�ֵ��Ϊ����ֵ
					finalresult.append(Integer.toString(value) + ",");
				}
			}
			finalresult.append(Integer.toString(sum / ar[i - 1]));
       //     System.out.println(finalresult);
			
		}
	}
}