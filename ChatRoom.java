import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
public class ChatRoom 
{
	//服务器，用于转发收到客户端的消息
	//客户端列表，包装成线程安全的列表
	public static java.util.List<Socket> SocketList 
		= Collections.synchronizedList(new ArrayList<Socket>());
	public static void main(String[] args) throws Exception
	{
		//创建服务器服务
		ServerSocket ss=new ServerSocket(19988);
		//循环监听，等待客户端连接，一单链接则将客户端socket加入列表中
		//并启动一个线程为该客户端服务，继续监听等待下一个客户端连接
		while(true){
			System.out.println("服务器已启动，等待连接.....");
			Socket s=ss.accept();
			System.out.println(s.getInetAddress().getHostName()+"上线......");
			SocketList.add(s);
			System.out.println("当前房间人数为："+ChatRoom.SocketList.size());
			new Thread(new MyServer(s)).start();
		}
		//System.out.println("Hello World!");
	}
}

//服务器线程
class MyServer implements Runnable
{
	//封装一个客户端socket，利用socket与客户端通信
	private Socket s;
	public MyServer(Socket s) throws Exception
	{
		this.s=s;
	}

	@Override
		public void run(){
			try
			{
				BufferedReader br =
					new BufferedReader(new InputStreamReader(s.getInputStream()));
				String content=null;
				while((content=br.readLine())!=null)
				{
					//遍历客户端socket列表，将收到的客户端数据转发出去
					for(Socket socket:ChatRoom.SocketList)
					{
						//不向发来数据的客户端回发
						if(s!=socket){
							PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
							pw.println(content);
						}
							
					}
						
				}
			}
			catch (Exception e)
			{
				System.out.println(s.getInetAddress().getHostName()+"已下线");
				ChatRoom.SocketList.remove(s);
				System.out.println("当前房间人数为："+ChatRoom.SocketList.size());
				//throw new RuntimeException("error");
			}
	}
}

class MyClient 
{
	//制作客户端GUI
	private Frame frame;
	//会话区域和输入区域
	private TextArea inputArea,chatArea;
	//发送按钮
	private Button send;
	//封装客户端socket
	private Socket s;
	public MyClient()throws Exception{
		init();
	}
	//初始化操作
	public void init() throws Exception{
		//创建客户端的socket，与服务器连接
		s= new Socket("127.0.0.1",19988);
		//创建会话区
		chatArea= new TextArea(20,65);
		//启动一个线程专门用于接收服务器的消息
		new Thread(new Client(s,chatArea)).start();

		frame = new Frame("聊天窗口");
		//frame.setBounds(300,200,500,500);
		//frame.setLayout(new FlowLayout());
		
		
		frame.add(chatArea);

		inputArea=new TextArea(5,60);
		Panel panel = new Panel();
		panel.setLayout(new FlowLayout());
		panel.add(inputArea);

		send=new Button("发送");
		panel.add(send);
		//panel.pack();
		frame.add(panel,BorderLayout.SOUTH);
		MyEvents();
		frame.pack();
		frame.setVisible(true);
	}

	private void MyEvents()throws Exception {
		//窗口关闭
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
		
		//点击发送按钮发送
		send.addActionListener(new ActionListener(){
			@Override
				public void actionPerformed(ActionEvent e){
				try
				{
					sendMsg();
				}
				catch (Exception ex)
				{
				}
				
			}
		});


		//点击回车发送
		inputArea.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
			if(e.getKeyChar()==Event.ENTER)
				try
				{
					sendMsg();
					e.consume();
				}
				catch (Exception ex)
				{
				}
				
			 }
		});

		//对话框不能进行输入
		chatArea.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
				e.consume();
			}
		});

	}


	private void sendMsg()throws Exception{
		//发送消息
		PrintWriter pw = new PrintWriter(s.getOutputStream(),true);
		String content = inputArea.getText();
		content=content.trim();
		//将输入区域中的内容发送到客户端并清空输入区，发送到本端的会话区域
		if(content.equals(""))
			return;
		pw.println(s.getLocalAddress().getHostName()+"说："+content);
		inputArea.setText("");
		chatArea.append("我说："+content+"\r\n");
	}



	public static void main(String[] args) throws Exception
	{

		new MyClient();		
	}
}





class Client implements Runnable
{
	Socket s;
	TextArea ta;
	public Client(Socket s,TextArea ta) throws Exception
	{
		this.s=s;
		this.ta=ta;
	}
	@Override
		public void run(){
		try
		{
			BufferedReader br =
					new BufferedReader(new InputStreamReader(s.getInputStream()));

			String content=null;
			while((content=br.readLine())!=null)
			{
				//System.out.println(content);
				ta.append(content+"\r\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	
		
	}
}