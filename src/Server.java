import java.io.*;
import java.net.*;
import java.util.Date;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Server extends JFrame{
	
	private JTextField userText; //user input
	private JTextArea chatWin; //chat window
	private ObjectOutputStream output; //outgoing data
	private ObjectInputStream input; //incoming data
	private ServerSocket server; //server socket
	private Socket conn; //connection socket
	
	private RandomAccessFile raf; //Just having fun trying out making a log 
	
	public Server() {
		super("Instant messenger - Server");
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e){
							sendMessage(userText.getText());
							userText.setText("");
						}					
					});
		add(userText, BorderLayout.NORTH);
		chatWin = new JTextArea();
		chatWin.setEditable(false);
		add(new JScrollPane(chatWin));
		setBounds(0,0,300,150);
		setVisible(true);
	}
	//setup and run server
	public void startRunning(){
		try{
			server = new ServerSocket(13337, 100);//65535 is limit
			while(true){
				try{
					//connect and have conversation
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException ex){
					showMessage("\nServer ended the connection!");
				}finally{
					closeCrap();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	//wait for a connection, then display connection information
	private void waitForConnection() throws IOException{
		raf = new RandomAccessFile("spillage.txt", "rw");
		if(raf.length() > 0){//does the file have anything in it?
			raf.seek(raf.length() - 1);
		}
		raf.writeUTF("" + new Date().toString());
		
		showMessage("Waiting for connection... \n");
		conn = server.accept();
		showMessage("Now connected with " + conn.getInetAddress().getHostName() + "\n");
	}
	//get stream to send and receive data
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(conn.getOutputStream());
		output.flush();//get rid of leftover data
		input = new ObjectInputStream(conn.getInputStream());
		showMessage("Streams now setup.\n");
	}
	//during the conversation
	private void whileChatting() throws IOException{
		String msg = "You are now connected\n";
		sendMessage(msg);
		ableToType(true);
		do{
			try{
				System.out.println(input.read());
				msg = (String)input.readObject();
				showMessage(msg);
			}catch(ClassNotFoundException exx){
				showMessage("\n message unknown \n");
			}
		}while(!(msg.split(":")[1].equals(" END")));
	}
	//close streams and sockets
	public void closeCrap(){
		showMessage("\nClosing connections...");
		ableToType(false);
		try{
			output.close();
			input.close();
			conn.close();
			raf.close();
		}catch(IOException ioex){
			ioex.printStackTrace();
		}
	}
	//send message to client
	private void sendMessage(String msg){
		try{
			output.writeObject("\nSERVER: " + msg);
			output.flush();
			showMessage("\nSERVER: " + msg);
		}catch(IOException iooe){
			chatWin.append("\nERROR UNABLE TO SEND!");
		}
	}
	//update chat window. TextArea.append updates the text area.
	private void showMessage(final String text){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWin.append(text);
					try {
						raf.writeUTF(text);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		);
	}
	//enable the textbox thing to be editable
	private void ableToType(final boolean val){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(val);
				}	
		});
	}
}