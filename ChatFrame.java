/*new comment*/
import java.awt.CheckboxMenuItem;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;


public class ChatFrame extends JFrame {
	Thread s;
	private JEditorPane textarea, userarea;
	private static boolean FLAG_LOG_CHECKED = false;
	public DefaultCaret caret, usercaret;
	public ArrayList<String> usrlst;
	public String name = null;
	public String address = null;
	public String s_port = null;
	public Integer i_port;
	JFrame frame;
	private JPopupMenu popup = new JPopupMenu();
	private JMenuItem copy = new JMenuItem("Copy");
	private JMenuBar menu = new JMenuBar();
	private JMenu propert = new JMenu("Properties");
	private JMenu about = new JMenu("Info");
	private JMenuItem aboutitem = new JMenuItem("About");
	private JCheckBoxMenuItem logitem = new JCheckBoxMenuItem("Log to file");
	private JTextField type = new JTextField();
	DatagramSocket dsend;
	DatagramPacket packet;
	DatagramSocket dres;
	Socket skt;
	private JButton send = new JButton("Send");
	private JButton disconnect = new JButton("Offline");
	private JButton connect = new JButton("Online");
    
	ChatFrame()
	{
		super("Chat");
		
		frame = new JFrame("Chat");
		frame.setLayout(null);
		frame.setBounds(200, 250, 600, 350);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new MyWindListener());

        type.setBounds(8,220,500,30);
		menu.setBounds(0, 0, 600, 15);
	    about.add(aboutitem);
	    propert.add(logitem);
	    logitem.addItemListener(new CheckBoxListener());
	    aboutitem.addActionListener(new MenuItemListener());
		menu.add(propert);
		menu.add(about);
		frame.add(menu);
		frame.add(send);
		frame.add(disconnect);
		frame.add(connect);
        frame.add(type);
		Font fontmain = new Font("SansSerif", Font.PLAIN, 16);
		
			
		textarea = new JEditorPane();
		userarea = new JEditorPane();
		userarea.setText("Users in chat:");
		  // textarea.setContentType("text/html"); 
		//textarea.setFont(fontmain);
		textarea.setEditable(false);
		textarea.setEditable(false);		
		caret = (DefaultCaret)textarea.getCaret();
		usercaret = (DefaultCaret)userarea.getCaret();		
		JScrollPane scroll = new JScrollPane(textarea);
		JScrollPane userscroll = new JScrollPane(userarea);
		scroll.setBounds(8, 15, 450, 200);
		userscroll.setBounds(460, 15, 127, 200);	
		//textarea.setLineWrap(true);
		scroll.setAutoscrolls(true);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		userscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		frame.add(scroll);
		frame.add(userscroll);
		textarea.addMouseListener(new MyMouseListener());
	    type.addKeyListener(new MyKeyListener());
	    copy.addActionListener(new PopupListener());
		/*buton*/
	    connect.addActionListener(new ButtonListener());
		connect.setBounds(90, 250, 78, 30);
		connect.setEnabled(false);
	    disconnect.addActionListener(new ButtonListener());
		disconnect.setBounds(8, 250, 78, 30);
		send.addActionListener(new ButtonListener());
		send.setBounds(500, 220, 78, 30);
        LogDialog dialog = new LogDialog();
        frame.validate();		
		OnConnect();
	}
	
	/************************************MAIN MENU LISTENER**************************/
	class MenuItemListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent a) 
		{
			if (a.getActionCommand().equals("About"))
			{
				JOptionPane.showMessageDialog(frame, "<html><h3>This is simple client-server LAN chat application</h3> <br> <b>by Pavel Ushakov.</b></html>");
			}	
		}	
	}
	
	public void OnConnect()
	{
		PrintWriter out = null;
		
		String message = "#" + name + "# " + "is connected";
		try 
		{
		 skt = new Socket(address, Integer.parseInt(s_port));
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(frame, "Listen port failed or server is off");
			e.printStackTrace();
			//skt.close();
			System.exit(0);
		}
		try {
			  out = new PrintWriter(skt.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		         out.println(message);
		         out.flush();
		s = new Thread(new DatagramGet());
	    s.start();
	}
	
	public void OnDisconnect()
	{
		PrintWriter out = null;
		String text;
		text = "@" + name + "@";
		try 
		{
			out = new PrintWriter(skt.getOutputStream(), true);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(0);
		}
		out.println(text);
		out.flush();
		type.setText("");	
		try {
			skt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		s.interrupt();
	}
	
	
	/***********************CHECK BOX ITEM LISTENER***************************/
	class CheckBoxListener implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent a) {
			if (a.getStateChange() == ItemEvent.SELECTED )
			{
				FLAG_LOG_CHECKED = true;
			}
			else
				FLAG_LOG_CHECKED = false;
		}	
	}
	
	public void LogToFile(String str)
	{
		if (FLAG_LOG_CHECKED)
		{
		File f = new File("lg.txt");
		FileWriter file;
		try {
			file = new FileWriter(f, true);
			BufferedWriter bw = new BufferedWriter(file);
			PrintWriter out = new PrintWriter(bw);
			out.println(str);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		}
		
	}
	
	/*****************WINDOW LISTENER*****************************************/
	class MyWindListener implements WindowListener
	{
		@Override
		public void windowActivated(WindowEvent arg0) {
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
		}

		@Override
		public void windowClosing(WindowEvent a) {
			OnDisconnect();	
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {	
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
		}

		@Override
		public void windowOpened(WindowEvent arg0) {	
		}	
	}
	
	/***********************SEND BUTTON LISTENER*******************************/
	class ButtonListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent a) {
			PrintWriter out = null;
			if (a.getActionCommand().equals("Send"))
			{
				String text = type.getText();
				/* get time*/
				SimpleDateFormat time = new SimpleDateFormat("HH:mm");
				String t = time.format(new Date());
				/************/
				text = "["+t+"] " + name + "> " + text;
				try 
				{
					out = new PrintWriter(skt.getOutputStream(), true);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			
			    out.println(text);
			    out.flush();
			    type.setText("");
			    LogToFile(text);
			}	
			if (a.getActionCommand().equals("Offline"))
			{
				OnDisconnect();
				connect.setEnabled(true);
				disconnect.setEnabled(false);
			}
			if (a.getActionCommand().equals("Online"))
			{
				OnConnect();
				connect.setEnabled(false);
				disconnect.setEnabled(true);
			}
		}	
	}	
	
	/***********************POPUP MENU LISTENER******************************************/
	class PopupListener implements ActionListener /* action for popup menu "copy" */
	{

		@Override
		public void actionPerformed(ActionEvent a) {
			if (a.getActionCommand().equals("Copy"))
			{
				StringSelection ss = new StringSelection(textarea.getSelectedText()); /* getselected string */
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null); /* put string to clipboard */
			}		
		}	
	}
	
	/*******************************MOUSE LISTENER FOR TEXT SELECTION*****************************/
    class MyMouseListener implements MouseListener
    {

		@Override
		public void mouseClicked(MouseEvent a) {
		if (a.getButton() == MouseEvent.BUTTON3)
		{
			popup.add(copy);
			Point p =  a.getPoint();
			popup.show(textarea, p.x, p.y);
		}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
	
		}

		@Override
		public void mouseExited(MouseEvent arg0) {

		}

		@Override
		public void mousePressed(MouseEvent arg0) {

		}

		@Override
		public void mouseReleased(MouseEvent arg0) {

		}	
    }
    
    /*************************ENTER KEY LISTENER*****************************/
	class MyKeyListener implements KeyListener
	{
		@Override
		public void keyPressed(KeyEvent k)
		{
			if ( k.getKeyCode() == KeyEvent.VK_ENTER)
			{
				PrintWriter out = null;
				
					String text = type.getText();
					text = name + "> " + text;
					try 
					{
						out = new PrintWriter(skt.getOutputStream(), true);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				    out.println(text);
				    out.flush();
				    type.setText("");	
			}
		}
		
		@Override
		public void keyReleased(KeyEvent k) 
		{
		}
		
		@Override
		public void keyTyped(KeyEvent k)
		{
		}	
	}	
	
	
	public String ParseUserString(String s)
	{
		String userstr;
	    String textstr;
	    usrlst = new ArrayList<String>();
		StringTokenizer token = new StringTokenizer(s,"#");
		userstr = token.nextToken();
        textstr = token.nextToken();
        usrlst.add(userstr);
        for (String user : usrlst)
        {
        	userarea.setText(userarea.getText() + '\n' + user);
        }
		return userstr + textstr ;	
	}
	
	public String ParseString(String s)
	{	
	    	StringTokenizer token = new StringTokenizer(s,"~");
			String userlist = token.nextToken();     
	        return userlist;
	}
	
	class DatagramGet implements Runnable
	{
		public void run()
		{
			int n;
			byte []buf;
			String message = null;
			while(true)
			{
					try 
			    	{        
						DataInputStream in = new DataInputStream(skt.getInputStream());
					    buf = new byte[1500];
					    while ((n = in.read(buf,0,1500)) > -1)
					    {
					    	message = new String(buf);
					    	//JOptionPane.showMessageDialog(frame, message.trim());
					    	if ( message.indexOf('~') == 0 )
					    	{
					    		String online = "Users in chat:\n";
					    		online += ParseString(message.trim());
					    		userarea.setText(online);
					    		message = null;
					    		buf  = new byte[1500];
					    		continue;
					    	}

					    	textarea.setText(textarea.getText().trim() + '\n' + message.trim());
					    	//textarea.setText("<html><div><h4>My First Heading</h4>test</div></html>");
					    	caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
					    	message = null;
					    	buf = new byte[1500];
					    }
			    	}
			    	catch (IOException e)
			    	{	
			    	  e.printStackTrace();
			    	  break;
			    	}	
				}	
		}
	}
	
	/*****************************************LOGIN DIALOG WINDOW************************************/
	class LogDialog extends JDialog
	{
		LogDialog obj = this;
		JTextField login = new JTextField();
		JTextField ip = new JTextField();
		JTextField port = new JTextField();
		JButton ok = new JButton("Ok");
		JButton ex = new JButton("Cancel");
		JLabel loginL = new JLabel("Login:");
		JLabel ipL = new JLabel("<html><h5>Serv.addr:</h5></html>");
		JLabel portL = new JLabel("Port:");
		LogDialog()
		{
			super();
			this.setDefaultCloseOperation(LogDialog.DO_NOTHING_ON_CLOSE);
			this.setTitle("Login");
			this.setLayout(null);
			this.setBounds(400, 400, 280, 200);
			this.setResizable(false);
			loginL.setBounds(5, 10, 60, 20);
			ipL.setBounds(5, 40, 60, 20);
			portL.setBounds(5, 70, 60, 20);
			login.setBounds(80, 10, 120, 20);
			ip.setBounds(80, 40, 120, 20);
			port.setBounds(80, 70, 120, 20);
			ok.setBounds(50, 100, 90, 30);
			ex.setBounds(160, 100, 90, 30);
			Properties p = System.getProperties();
			login.setText(p.getProperty("user.name"));
			ip.setText("127.0.0.1");
			port.setText("30000");
			this.add(login);
			this.add(ip);
			this.add(port);
			this.add(ok);
			this.add(ex);
			this.add(loginL);
			this.add(ipL);
			this.add(portL);
			ok.addActionListener(new ButtonListener());
			ex.addActionListener(new ButtonListener());
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			this.setVisible(true);	
		}
		
		class ButtonListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent a) 
			{
				if (a.getActionCommand().equals("Ok"))
				{
					if ((name = login.getText()).equals("") || (address = ip.getText()).equals("") || (s_port = port.getText()).equals(""))
					{
						JOptionPane.showMessageDialog(frame, "Input all data!", "Warning!", JOptionPane.WARNING_MESSAGE);
					}
					else 
						{
							obj.setVisible(false);
							frame.setTitle(name);
						}
				}
				else if (a.getActionCommand().equals("Cancel"))
				{
					System.exit(0);
				}
			}	
		}	
	}	

		
/*********************************************MAIN*****************************************/	
	public static void main(String[] args)
	{
		ChatFrame chat = new ChatFrame();
	}
}


		
	
	


