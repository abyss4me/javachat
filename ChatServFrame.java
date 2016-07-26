/*new comment, git check */
/*another comment*/
/* sme fixes*/
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class ChatServFrame 
{
	public static ArrayList<Socket> lst;
	public static ArrayList<String> usrlst;
	public static ArrayList<Thread> threads;
	public static Vector<Socket> vect;
	
	//public static List lst;
	JFrame frame;
	public JTextArea textarea;
	public DefaultCaret caret;
	ServerSocket socket;
	DatagramSocket datagram;


	DatagramPacket packet;
	ChatServFrame()
	{
		super();
		frame = new JFrame("Server");
		frame.setLayout(null);
		frame.setBounds(400, 200, 400, 300);
	    frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		textarea = new JTextArea();
		textarea.setEditable(false);
		caret = (DefaultCaret)textarea.getCaret();
		JScrollPane scroll = new JScrollPane(textarea);
		scroll.setBounds(2, 2, 390, 200);
		scroll.setAutoscrolls(true);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		frame.add(scroll);
		JButton start = new JButton("Start");
		JButton stop = new JButton("Stop");
		JButton clear = new JButton("Clear");
		start.setBounds(10,220,70,30);
		stop.setBounds(130,220,70,30);
		clear.setBounds(260,220,70,30);
		start.addActionListener(new ButtonListener());
		stop.addActionListener(new ButtonListener());
		clear.addActionListener(new ButtonListener());
		frame.add(start);
		frame.add(stop);
		frame.add(clear);
		frame.setVisible(true);
		//frame.validate();
		
		lst = new ArrayList<Socket>();
		usrlst = new ArrayList<String>();
		threads = new ArrayList<Thread>();
		
		
		try
		{
			socket = new ServerSocket(30000);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		textarea.setText(textarea.getText() +  "Server is running...");
		Thread listen = new Thread(new RunThread());
		listen.start();
	}
	public String GetCurUserName(String s, String c)
	{
		String username;
	    	StringTokenizer token = new StringTokenizer(s,c);
			username = token.nextToken();
		return username;		
	}
	public String ParseUserString(String s)
	{
		String userstr;
	    String total = "~";
	    if (s.indexOf('@') == 0 )
	    {
	    	StringTokenizer token = new StringTokenizer(s,"@");
			userstr = token.nextToken();
	        usrlst.remove(userstr);
	        for ( String user : usrlst)
	        {
	        	total +=  user + '\n';
	        	//total+=user;
	        }
	        userstr = null;     
	        return total;
	    }
	    
	    if (s.indexOf('#') == 0 )
	    {
	    	StringTokenizer token = new StringTokenizer(s,"#");
			userstr = token.nextToken();
			usrlst.add(userstr);
			for ( String user : usrlst)
			{
				total +=  user + '\n';
        	//total+=user;
			}
			return total;
	    }
		return null;
	}
	
	public static void initArray(Socket s)
	{
		lst.add(s);
	}
	class ClientsThread implements Runnable
	{
		Socket skt;
		ClientsThread(Socket s)
		{	
			skt = s;
			initArray(s);
		}
		@Override
		public void run() 
		{
			int n=0;
			byte []buf;
			String message = null;
			while(true)
			{
				try
				{
					DataInputStream in = new DataInputStream(skt.getInputStream());
				    buf  = new byte[1500];
				    while ((n=in.read(buf,0,1500)) > -1)
				    {   	
				    	message = new String(buf);
				    	if ( message.indexOf('#') == 0 ) /* # - token determines ONLINE for user */
				    	{								/* here we send User's names which are Online to the client */
				    		String list = ParseUserString(message);
				    		for (int i=0; i<=lst.size()-1; i++)
					    	{
					    	    //if (lst.get(i).getRemoteSocketAddress() == null)
					    	    	//continue;
					    		PrintWriter out = new PrintWriter(lst.get(i).getOutputStream(), true); 
					    		out.println(list); /*send list of users who enter to chat to the client*/
					    		/* suspend thread for 1 sec in order to wait between to sends of message */
					    		try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
					    		/*************************************************************************/
					    		out.println(GetCurUserName(message.trim(),"#") + " is ONLINE"); /*send status offline for current user*/
					    		SocketAddress addr = skt.getRemoteSocketAddress();
					    		textarea.setText(textarea.getText() + '\n' + message.trim() + " " + addr.toString());
					    		//out.flush();
					    	}	
					    	buf = new byte[1500];
				    		continue;
				    	}
				    	
				    	if ( message.indexOf('@') == 0 ) /* @ - token signals that user is OFFLINE */
				    	{								/* here we sends the new user's list without current user who is just off*/
				    		String list = ParseUserString(message);
				    		for (int i=0; i<=lst.size()-1; i++)
					    	{
					    	    if (lst.get(i).getRemoteSocketAddress() == null) //lst.get(i).isClosed()
					    	    {
					    	    	lst.remove(i);
					    	    	continue;
					    	    }
					    		PrintWriter out = new PrintWriter(lst.get(i).getOutputStream(), true); 	    		
					    		out.println(list);
					    		out.println(GetCurUserName(message.trim(),"@") + " is OFFLINE");
					    		textarea.setText(list);
					    	}
				    		list = null;
				    		message = null;		    				    	
				    		try
				    		{
								Thread.currentThread().join();
							} 
				    		catch (InterruptedException e) 
				    		{
								e.printStackTrace();
							}
				    	}
			    	
				    	for (int i=0; i<=lst.size()-1; i++) /* here we send to all clients messages */
				    	{
				    	    //if (lst.get(i).getRemoteSocketAddress() == null)
				    	    	//continue;
				    		PrintWriter out = new PrintWriter(lst.get(i).getOutputStream(), true); 
				    		out.println(message);
				    		out.flush();
				    	}	
				    	
				    	
				    	textarea.setText(textarea.getText() + '\n' + message.trim());
				    	caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
				    	message = null; 
				    	buf = new byte[1500];	
				    }
				}
				catch (IOException e) 
				{
					textarea.setText("client disconnected\n");
					//Thread.currentThread().interrupt();
					try {
						skt.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();				
					break;
				}
			}	
		}	
	}
	
	class RunThread implements Runnable
	{	
		Socket skt;
		@Override
		public void run() 
		{
			while(true)
			{
				try 
				{
					skt = socket.accept();
					Thread client = new Thread(new ClientsThread(skt));
					threads.add(client);
					client.start();
				} 	
				catch (IOException e) 
				{
					e.printStackTrace();
				}		
			}
		}
	}
	
	class ButtonListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent a) {
			if ( a.getActionCommand().equals("Start"))
			{
				
			} else
				if ( a.getActionCommand().equals("Stop"))
				{
					//System.exit(0);
					for (Thread t : threads)
					{
						t.interrupt();
					}
				} else
					if ( a.getActionCommand().equals("Clear"))
					{
						textarea.setText("");
					}
			
			
		}
		
	}
	
	public static void main(String[]argc)
	{
		
		ChatServFrame serv = new ChatServFrame();
		
		
	}
}
