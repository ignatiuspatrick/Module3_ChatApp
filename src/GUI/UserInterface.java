package GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import RoutingProtocol.Message;
import RoutingProtocol.RoutingProtocol;
//import RoutingProtocol.FileTransfer;

public class UserInterface implements Observer {
	private final JTextPane jtextFilDiscu = new JTextPane();
	private final JTextPane jtextListUsers = new JTextPane();
	private final JTextField jtextInputChat = new JTextField();
	private String oldMsg = "";
	private Thread read;
	private String serverName;
	private int PORT;
	private String name;
	private String password;
	private BufferedReader input;
	private PrintWriter output;
	private Socket server;
	//private RoutingProtocol client;

	//private FileTransfer file;
	private String message;
	private Startup start;

	// ------------Login page interface--------------

	public UserInterface(Startup s) {
		this.start = s;
		this.serverName = "Group5";
		this.PORT = 0;
		this.name = "Enter Name";
		this.password = "Enter Password";

		String fontfamily = "Arial, sans-serif";
		Font font = new Font(fontfamily, Font.PLAIN, 15);

		final JFrame jfr = new JFrame("Chat");
		jfr.getContentPane().setLayout(null);
		jfr.setSize(700, 600);
		jfr.setResizable(false);
		jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Module chat area
		jtextFilDiscu.setBounds(183, 25, 492, 320);
		jtextFilDiscu.setFont(font);
		jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
		jtextFilDiscu.setEditable(false);
		JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
		jtextFilDiscuSP.setBounds(183, 25, 492, 320);

		jtextFilDiscu.setContentType("text/html");
		jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		// Module of list of users
		jtextListUsers.setBounds(25, 25, 156, 320);
		jtextListUsers.setFont(font);
		jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
		jtextListUsers.setEditable(false);
		JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
		jsplistuser.setBounds(25, 25, 156, 320);

		jtextListUsers.setContentType("text/html");
		jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		// Login page view
		final JTextField jtfName = new JTextField(this.name);
		final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
		final JTextField jtfAddr = new JTextField(this.serverName);
		final JTextField jtfpassword = new JTextField(this.password);
		final JButton jcbtn = new JButton("Connect");

		// check if those field are not empty
		jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jtfpassword, jcbtn));
		jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jtfpassword, jcbtn));
		jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jtfpassword, jcbtn));
		jtfpassword.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jtfpassword, jcbtn));

		// position of Modules
		jcbtn.setFont(font);
		jtfAddr.setBounds(25, 380, 135, 40);
		jtfName.setBounds(375, 380, 135, 40);
		jtfport.setBounds(200, 380, 135, 40);
		jcbtn.setBounds(575, 380, 100, 40);
		jtfpassword.setBounds(25, 425, 650, 40);

		// color of chat modules and user list
		jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
		jtextListUsers.setBackground(Color.LIGHT_GRAY);

		// adding elements
		jfr.add(jcbtn);
		jfr.add(jtextFilDiscuSP);
		jfr.add(jsplistuser);
		jfr.add(jtfName);
		jfr.add(jtfport);
		jfr.add(jtfAddr);
		jfr.add(jtfpassword);
		jfr.setVisible(true);

		// info of chat application
		appendToPane(jtextFilDiscu, "<h2><b>WELCOME TO GROUP 5 CHAT</b></h2>");

		// -------------Interface after connection established-------------

		// Field message user input
		jtextInputChat.setBounds(0, 350, 400, 50);
		jtextInputChat.setFont(font);
		jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
		final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
		jtextInputChatSP.setBounds(25, 350, 650, 50);

		// button send
		final JButton jsbtn = new JButton("Send");
		jsbtn.setFont(font);
		jsbtn.setBounds(575, 410, 100, 35);

		// button Disconnect
		final JButton jsbtndeco = new JButton("Disconnect");
		jsbtndeco.setFont(font);
		jsbtndeco.setBounds(25, 410, 130, 35);

		jtextInputChat.addKeyListener(new KeyAdapter() {
			// send message on Enter
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMessage();
				}

				// Get last message typed
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					String currentMessage = jtextInputChat.getText().trim();
					jtextInputChat.setText(oldMsg);
					oldMsg = currentMessage;
				}

				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					String currentMessage = jtextInputChat.getText().trim();
					jtextInputChat.setText(oldMsg);
					oldMsg = currentMessage;
				}
			}
		});

		// Click on send button
		jsbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sendMessage();
			}
		});

		// when connect button is pressed
		jcbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
					name = jtfName.getText();
					String port = jtfport.getText();
					serverName = jtfAddr.getText();
					PORT = Integer.parseInt(port);
					password = jtfpassword.getText();

					appendToPane(jtextFilDiscu,
							"<span>Connecting to " + serverName + " with computer number " + PORT + "...</span>");
					if (start.passValid(password)) {
						appendToPane(jtextFilDiscu,"<span>Connected to group 5</span>");
						//appendToPane(jtextListUsers, "<span>ONLINE" + "/n" +  + "</span>");
						start.Connect((byte)PORT, password, name);
						jfr.remove(jtfName);
						jfr.remove(jtfport);
						jfr.remove(jtfAddr);
						jfr.remove(jcbtn);
						jfr.remove(jtfpassword);
						jfr.add(jsbtn);
						jfr.add(jtextInputChatSP);
						jfr.add(jsbtndeco);
						jfr.revalidate();
						jfr.repaint();
						jtextFilDiscu.setBackground(Color.WHITE);
						jtextListUsers.setBackground(Color.WHITE);
					} else {
						appendToPane(jtextFilDiscu, "<span>Could not connect to Server because</span>");
						JOptionPane.showMessageDialog(jfr, "Incorrect Password");
					}

			}

		});

		// when disconnect button is pressed
		jsbtndeco.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				jfr.add(jtfName);
				jfr.add(jtfport);
				jfr.add(jtfAddr);
				jfr.add(jtfpassword);
				jfr.add(jcbtn);
				jfr.remove(jsbtn);
				jfr.remove(jtextInputChatSP);
				jfr.remove(jsbtndeco);
				jfr.revalidate();
				jfr.repaint();
				read.interrupt();
				jtextListUsers.setText(null);
				jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
				jtextListUsers.setBackground(Color.LIGHT_GRAY);
				appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
				output.close();
			}
		});

		/*try {
			o = new RoutingProtocol((byte) client.getID(), this.password);
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/

	}

	// check if if all field are not empty
	public class TextListener implements DocumentListener {
		JTextField jtf1;
		JTextField jtf2;
		JTextField jtf3;
		JTextField jtf4;
		JButton jcbtn;

		public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JTextField jtf4, JButton jcbtn) {
			this.jtf1 = jtf1;
			this.jtf2 = jtf2;
			this.jtf3 = jtf3;
			this.jtf4 = jtf4;
			this.jcbtn = jcbtn;
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void removeUpdate(DocumentEvent e) {
			if (jtf1.getText().trim().equals("") || jtf2.getText().trim().equals("")
					|| jtf3.getText().trim().equals("")) {
				jcbtn.setEnabled(false);
			} else {
				jcbtn.setEnabled(true);
			}
		}

		public void insertUpdate(DocumentEvent e) {
			if (jtf1.getText().trim().equals("") || jtf2.getText().trim().equals("")
					|| jtf3.getText().trim().equals("")) {
				jcbtn.setEnabled(false);
			} else {
				jcbtn.setEnabled(true);
			}
		}

	}
	
	// send html to pane
	private void appendToPane(JTextPane tp, String msg) {
		HTMLDocument doc = (HTMLDocument) tp.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
		try {
			editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
		tp.setCaretPosition(doc.getLength());
	}

	//--------------Methods for sending and receiving messages to and from to the other layer------------
	
	// sending the new messages
	public void sendMessage() {
		message = jtextInputChat.getText().trim();
		if (message.equals("") || message == null) {
			return;
		}
		
		this.oldMsg = message;
		jtextInputChat.requestFocus();
		jtextInputChat.setText(null);
		Thread t = new Thread(new sendThread(start, message));
		t.start();
	}

	// return the message
	public String getMsg() {
		return this.message;
	}
	
	public String getPass() {
		return this.password;
	}

	@Override
	public void update(Observable arg0, Object obj) {
		System.out.println("Message received");
		if (obj instanceof Message) {
			Message m = (Message) obj;
			appendToPane(jtextFilDiscu, "<span>" + m.toString() + "</span>");
			System.out.println(m.toString());
		}
		if (obj instanceof Object[]) {
			for (Object p: (Object[]) obj) {
				appendToPane(jtextListUsers, "<span>" + (String) p + "</span>");
			}
			
		}
	}
	
	public class sendThread implements Runnable  {

		private Startup start;
		private String message;
		
		public sendThread(Startup s, String m) {
			start = s;
			message = m;
		}
		
		@Override
		public void run() {
			start.Send(message);
		}
		
	}
}