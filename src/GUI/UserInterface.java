package GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;
import java.util.Observable;
import java.util.Observer;
import RoutingProtocol.Message;
import GUI.Startup;

/*
 * This class handles the user interface of the chat application
 */

public class UserInterface implements Observer {
	private final JTextPane jtextFilDiscu = new JTextPane();
	private final JTextPane jtextListUsers = new JTextPane();
	private final JTextArea jtextInputChat = new JTextArea();
	private final JButton jsbtn = new JButton("Send");
	private final JButton jsbtndeco = new JButton("Disconnect");
	private final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
	private String oldMsg = "";
	private Thread read;
	private String serverName;
	private int PORT;
	private String name;
	private String password;
	private PrintWriter output;
	private String message;
	private Startup start;
	private String userHeader = "<h><b>USERS ONLINE</b></h>";

	// ------------Login page interface--------------

	// constructor
	public UserInterface(Startup s) {
		this.start = s;
		this.serverName = "Group5";
		this.PORT = 0;
		this.name = "Enter Name";
		this.password = "Enter Password";

		// font and size of the text in the interface
		String fontfamily = "Arial, sans-serif";
		Font font = new Font(fontfamily, Font.PLAIN, 24);
		Font msgfont = new Font(fontfamily, Font.PLAIN, 28);

		// chat application main frame
		final JFrame jfr = new JFrame("Chat");
		jfr.getContentPane().setLayout(null);
		jfr.setSize(1300, 900);
		jfr.setResizable(false);
		jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// chat area box
		jtextFilDiscu.setBounds(390, 25, 892, 520);
		jtextFilDiscu.setFont(font);
		jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
		jtextFilDiscu.setEditable(false);
		JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
		jtextFilDiscuSP.setBounds(390, 25, 892, 520);

		jtextFilDiscu.setContentType("text/html");
		jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		// list of users box
		jtextListUsers.setBounds(25, 25, 356, 520);
		jtextListUsers.setFont(font);
		jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
		jtextListUsers.setEditable(false);
		JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
		jsplistuser.setBounds(25, 25, 356, 520);

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

		// position of all the boxes
		jcbtn.setFont(font);
		jtfAddr.setFont(font);
		jtfName.setFont(font);
		jtfport.setFont(font);
		jtfpassword.setFont(font);
		jtfAddr.setBounds(25, 580, 305, 80);
		jtfName.setBounds(655, 580, 305, 80);
		jtfport.setBounds(340, 580, 305, 80);
		jcbtn.setBounds(970, 580, 305, 80);
		jtfpassword.setBounds(25, 700, 1250, 80);

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
		
		// connect if user presses enter after entering password
		jtfpassword.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					name = jtfName.getText();
					String port = jtfport.getText();
					serverName = jtfAddr.getText();
					PORT = Integer.parseInt(port);
					password = jtfpassword.getText();
	
					appendToPane(jtextFilDiscu,"<span>Connecting to " + serverName + " with computer number " + PORT + "...</span>");
					appendToPane(jtextFilDiscu,"<span>Connected to group 5</span>");
					appendToPane(jtextListUsers, userHeader);
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
				} else if(e.getKeyCode() == KeyEvent.VK_UP) {
					
				}
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
					appendToPane(jtextFilDiscu,"<span>Connected to group 5</span>");
					appendToPane(jtextListUsers, userHeader);
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
					//} else {
					//	appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
					//	JOptionPane.showMessageDialog(jfr, "Incorrect Password");
					//}

			}

		});

		// info of chat application
		appendToPane(jtextFilDiscu, "<h2><b>WELCOME TO GROUP 5 CHAT</b></h2>");

		// -------------Interface after connection established-------------

		// Field message box (user input)
		jtextInputChat.setBounds(0, 580, 1255, 100);
		jtextInputChat.setFont(msgfont);
		jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
		jtextInputChat.setLineWrap(true);
		jtextInputChat.setWrapStyleWord(true);
		JFrame f = new JFrame();
		f.getContentPane().add(jtextInputChatSP);
		jtextInputChatSP.setBounds(25, 580, 1255, 100);
		jtextInputChatSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		jtextInputChatSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// button send
		jsbtn.setFont(font);
		jsbtn.setBounds(975, 700, 300, 80);

		// button Disconnect
		jsbtndeco.setFont(font);
		jsbtndeco.setBounds(25, 700, 300, 80);

		// actions performed according to key presses in the user input box
		jtextInputChat.addKeyListener(new KeyAdapter() {
			
			// send message on Enter
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMessage();
				}

				// Get last message pressed
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					String currentMessage = jtextInputChat.getText().trim();
					jtextInputChat.setText(oldMsg);
					oldMsg = currentMessage;
				}

				// get last message typed even when down key pressed
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
		
		// append own message also to the discussion pane
		appendToPane(jtextFilDiscu, "<span>" + "<b>Me</b>" + ":" + message.toString()+ "</span>");
	}

	// listen for new messages from the other layer
	@Override
	public void update(Observable arg0, Object obj) {
		System.out.println("Message received");
		
		// if message is not a list, add it to the discussion pane with the username of the person sent in bold
		if (obj instanceof Message) {
			Message m = (Message) obj;
			String name = m.toString().split(":")[0];
			appendToPane(jtextFilDiscu, "<span>" + "<b>" + name + "</b>" + ":" + m.toString().split(":")[1] + "</span>");
			System.out.println(m.toString());
		}
		
		// is received message is a list, append it to the users online pane
		if (obj instanceof Object[]) {
			
			// first remove the list already in pane to avoid duplication 
			
			jtextListUsers.setText(userHeader);
			
			for (Object p: (Object[]) obj) {
				appendToPane(jtextListUsers, "<span>" + (String) p + "</span>");
			}
			
		}
	}
	
	// create a new thread
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