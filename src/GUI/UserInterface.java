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

public class UserInterface implements Observer {
	private final JTextPane jtextFilDiscu = new JTextPane();
	private final JTextPane jtextListUsers = new JTextPane();
	private final JTextArea jtextInputChat = new JTextArea();
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

	public UserInterface(Startup s) {
		this.start = s;
		this.serverName = "Group5";
		this.PORT = 0;
		this.name = "Enter Name";
		this.password = "Enter Password";

		String fontfamily = "Arial, sans-serif";
		Font font = new Font(fontfamily, Font.PLAIN, 24);
		Font msgfont = new Font(fontfamily, Font.PLAIN, 28);

		final JFrame jfr = new JFrame("Chat");
		jfr.getContentPane().setLayout(null);
		jfr.setSize(1300, 900);
		jfr.setResizable(false);
		jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Module chat area
		jtextFilDiscu.setBounds(390, 25, 892, 520);
		jtextFilDiscu.setFont(font);
		jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
		jtextFilDiscu.setEditable(false);
		JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
		jtextFilDiscuSP.setBounds(390, 25, 892, 520);

		jtextFilDiscu.setContentType("text/html");
		jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		// Module of list of users
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

		// position of Modules
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

		// info of chat application
		appendToPane(jtextFilDiscu, "<h2><b>WELCOME TO GROUP 5 CHAT</b></h2>");

		// -------------Interface after connection established-------------

		// Field message user input
		jtextInputChat.setBounds(0, 580, 1255, 100);
		jtextInputChat.setFont(msgfont);
		jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
		jtextInputChat.setLineWrap(true);
		jtextInputChat.setWrapStyleWord(true);
		final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
		jtextInputChatSP.setBounds(25, 580, 1255, 100);
		jtextInputChatSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		jtextInputChatSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// button send
		final JButton jsbtn = new JButton("Send");
		jsbtn.setFont(font);
		jsbtn.setBounds(975, 700, 300, 80);

		// button Disconnect
		final JButton jsbtndeco = new JButton("Disconnect");
		jsbtndeco.setFont(font);
		jsbtndeco.setBounds(25, 700, 300, 80);

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
		appendToPane(jtextFilDiscu, "<span>" + "<b>Me</b>" + ":" + message.toString()+ "</span>");
	}

	@Override
	public void update(Observable arg0, Object obj) {
		System.out.println("Message received");
		if (obj instanceof Message) {
			Message m = (Message) obj;
			String name = m.toString().split(":")[0];
			appendToPane(jtextFilDiscu, "<span>" + "<b>" + name + "</b>" + ":" + m.toString().split(":")[1] + "</span>");
			System.out.println(m.toString());
		}
		if (obj instanceof Object[]) {
			jtextListUsers.setText(userHeader);
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