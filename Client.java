
//Sean Walker - swalke30
//Bilal V 
//Matt B
//CS 342 Program 5 - Networked Chat with RSA Encryption/Decryption

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.swing.*;
import java.util.*;

public class Client extends JFrame implements ActionListener {
  // GUI items
  JButton sendButton;
  JButton connectButton;
  JTextField machineInfo;
  JTextField portInfo;
  JTextField message;
  JTextField sendMessageTo;
  JTextField enterUserName;
  JTextArea history;

  // Network Items
  boolean connected;
  Socket echoSocket;
  ObjectOutputStream out;
  ObjectInputStream in;
  String userName;
  Vector<String> userNameList;
  Vector<Key> publicKeyList;
  Key privateKey; //(d,n)
  Key publicKey; //(e,n)
  int numberOfUsers;

  // set up GUI
  public Client() {
    super("Echo Client");

    publicKeyList = new Vector<Key>();
    numberOfUsers = 0;
    userNameList = new Vector<String>();

    // get content pane and set its layout
    Container container = getContentPane();
    container.setLayout(new BorderLayout());

    // set up the North panel
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new GridLayout(7, 3));
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

    upperPanel.add(new JLabel("Send message to: "), JLabel.RIGHT);
    sendMessageTo = new JTextField("");
    message.addActionListener(this);
    upperPanel.add(sendMessageTo);

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

  } // end constructor

  public static void main(String args[]) {
    Client application = new Client();
    application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public Vector<String> collectRecipiants() {
    String[] namesEnteredInSendMessageToBox = sendMessageTo.getText().split(",");
    Vector<String> recipientList = new Vector<String>();

    for (String username : namesEnteredInSendMessageToBox) {
      recipientList.add(username);
    }

    return recipientList;
  }//end collectRecipiants()

  // handle button event
  public void actionPerformed(ActionEvent event) {
    if (connected && (event.getSource() == sendButton || event.getSource() == message)) {
      if (message.getText().equals("whoIsHere")) { //client sends string to find out who is in chat room
        System.out.println("sending who is here message from client");
        doSendMessage("whoIsHere");
      } else {
        //make encryption object and send it...
        String[] namesEnteredInSendMessageToBox = sendMessageTo.getText().split(",");
        Vector<String> recipientList = new Vector<String>();

        for (String username : namesEnteredInSendMessageToBox) {
          recipientList.add(username);
        }
        Encryption newEncryptedMessage = new Encryption(userName + ": " + message.getText(), recipientList,
            this.privateKey);
        try {
          System.out.println("sending message via out");
          out.writeObject(newEncryptedMessage);
          out.flush();
        } catch (IOException e) {
          history.insert("cant send ", 0);
        }
      }
    } else if (event.getSource() == connectButton) {
      doManageConnection();
    }
  }

  public Vector<String> retrieveAndParseRecipientList() {
    Vector<String> recipientList = new Vector<String>();
    return recipientList;
  }

  public void doSendMessage(Object messageObject) {
    try {
      out.writeObject(messageObject);
      out.flush();
    } catch (IOException e) {
      history.insert("Error in processing message ", 0);
    }
  }//end doSendMessage()

  public void doManageConnection() {
    if (connected == false) {
      String machineName = null;
      int portNum = -1;
      try {
        machineName = machineInfo.getText();
        userName = enterUserName.getText();
        portNum = Integer.parseInt(portInfo.getText());
        echoSocket = new Socket(machineName, portNum);

        out = new ObjectOutputStream(echoSocket.getOutputStream()); //causes freeze
        in = new ObjectInputStream(echoSocket.getInputStream());
        sendButton.setEnabled(true);
        connected = true;
        //to do: recieve user name from server
        Vector<Key> publicPrivateKeys = Encryption.generateKeys();
        this.publicKey = publicPrivateKeys.get(0);
        this.privateKey = publicPrivateKeys.get(1);
        String newUserString = "newUser: " + userName + "," + Long.toString(publicKey.getX()) + ","
            + Long.toString(publicKey.getY());
        System.out.println(newUserString);
        out.writeObject(newUserString);
        out.flush();
        connectButton.setText("Disconnect from Server");
        new recieveInput();
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

  class recieveInput extends Thread {
    //Socket thisSocket;
    //ObjectOutputStream out;
    //ObjectInputStream in;

    public recieveInput(/*Socket echoSocket*/) {
      //thisSocket = echoSocket;
      start();
    }

    public void run() {
      try {
        // Make connection and initialize streams

        // Process all messages from server, according to the protocol.
        Object input;
        while ((input = in.readObject()) != null) {
          if (input.getClass().equals(String.class)) {
            String inputString = (String) input;
            if (inputString.contains("newUser: ")) {
              history.insert(inputString, 0);

              //add key to key list here
              String[] elementsOfNewUserString = inputString.split(","); //newUser: <NAME>, first # of key, second # of key
              userNameList.add(elementsOfNewUserString[0].substring(9)); //add user name to username list
              numberOfUsers++;
              long x = Long.parseLong(elementsOfNewUserString[1]);
              long y = Long.parseLong(Character.toString(elementsOfNewUserString[2].charAt(0)));
              publicKeyList.add(new Key(x, y));
            } else {
              history.insert(inputString, 0);
            }

          } else {
            Encryption inputEncryption = (Encryption) input;
            history.insert(inputEncryption.getMessage() + "\n", 0);
          }
        } //end while ((input = in.readObject()) != null)
      } catch (Exception e) {
        System.out.println(e);
      }
    }//end run()
  }//end recieveInput()
} // end class Server