
//Sean Walker - swalke30
//Bilal V
//CS 342 Program 5 - Networked Chat with RSA Encryption/Decryption

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame implements ActionListener {
  // GUI items
  JButton sendButton;
  JButton connectButton;
  JTextField machineInfo;
  JTextField portInfo;
  JTextField message;
  JTextField enterUserName;
  JTextArea history;

  // Network Items
  boolean connected;
  Socket echoSocket;
  PrintWriter out;
  BufferedReader in;
  String userName;

  // set up GUI
  public Client() {
    super("Echo Client");

    // get content pane and set its layout
    Container container = getContentPane();
    container.setLayout(new BorderLayout());

    // set up the North panel
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new GridLayout(6, 1));
    container.add(upperPanel, BorderLayout.NORTH);

    // create buttons
    connected = false;

    upperPanel.add(new JLabel("User Name: ", JLabel.CENTER));
    enterUserName = new JTextField("");
    enterUserName.addActionListener(this);
    upperPanel.add(enterUserName);

    upperPanel.add(new JLabel("Message: ", JLabel.LEFT));
    message = new JTextField("");
    message.addActionListener(this);
    upperPanel.add(message);

    sendButton = new JButton("Send Message");
    sendButton.addActionListener(this);
    sendButton.setEnabled(false);
    upperPanel.add(sendButton);

    connectButton = new JButton("Connect to Server");
    connectButton.addActionListener(this);
    upperPanel.add(connectButton);

    upperPanel.add(new JLabel("Server Address: ", JLabel.RIGHT));
    machineInfo = new JTextField("127.0.0.1");
    upperPanel.add(machineInfo);

    upperPanel.add(new JLabel("Server Port: ", JLabel.RIGHT));
    portInfo = new JTextField("");
    upperPanel.add(portInfo);

    history = new JTextArea(10, 40);
    history.setEditable(false);
    container.add(new JScrollPane(history), BorderLayout.CENTER);

    setSize(500, 250);
    setVisible(true);

  } // end CountDown constructor

  public static void main(String args[]) {
    Client application = new Client();
    application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  // handle button event
  public void actionPerformed(ActionEvent event) {
    if (connected && (event.getSource() == sendButton || event.getSource() == message)) {
      doSendMessage(userName + ": " + message.getText());
    } else if (event.getSource() == connectButton) {
      doManageConnection();
    }
  }

  public void doSendMessage(String text) {
    //try
    //{
    out.println(text);
    //history.insert(text, 0);
    //}
    //catch (IOException e)
    //{
    //history.insert ("Error in processing message ", 0);
    //}
  }

  public void doManageConnection() {
    if (connected == false) {
      String machineName = null;
      int portNum = -1;
      try {
        machineName = machineInfo.getText();
        userName = enterUserName.getText();
        portNum = Integer.parseInt(portInfo.getText());
        echoSocket = new Socket(machineName, portNum);
        out = new PrintWriter(echoSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        sendButton.setEnabled(true);
        connected = true;
        doSendMessage("newUser: " + userName);
        connectButton.setText("Disconnect from Server");
        new recieveInput(echoSocket);
      } catch (NumberFormatException e) {
        history.insert("Server Port must be an integer\n", 0);
      } catch (UnknownHostException e) {
        history.insert("Don't know about host: " + machineName, 0);
      } catch (IOException e) {
        history.insert("Couldn't get I/O for " + "the connection to: " + machineName, 0);
      }

    } else {
      try {
        out.close();
        in.close();
        echoSocket.close();
        sendButton.setEnabled(false);
        connected = false;
        connectButton.setText("Connect to Server");
      } catch (IOException e) {
        history.insert("Error in closing down Socket ", 0);
      }
    }
  }//end doManageConnection()

  class recieveInput extends Thread{
    Socket thisSocket;
    PrintWriter out;
    BufferedReader in;

    public recieveInput(Socket echoSocket){
      thisSocket = echoSocket;
      start();
    }
    public void run() {
      try{
          // Make connection and initialize streams
          BufferedReader in = new BufferedReader(new InputStreamReader(thisSocket.getInputStream()));
          PrintWriter out = new PrintWriter(thisSocket.getOutputStream(), true);
      
          // Process all messages from server, according to the protocol.
          System.out.println("here");
          String inputLine;
            while ((inputLine = in.readLine()) != null) {
              //inputLine = in.readLine();
              history.append(inputLine + "\n");
            }
        }catch(IOException e){
          System.out.println("exception");
        }
        }
  }
  


} // end class Server
