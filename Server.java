
//Sean Walker - swalke30
//Bilal Vijha - 
//Matt B
//CS 342 Program 5 - Networked Chat with RSA Encryption/Decryption

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.net.ssl.ExtendedSSLSession;
import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Vector;

public class Server extends JFrame implements ActionListener {
  // GUI items
  JButton ssButton;
  JLabel machineInfo;
  JLabel portInfo;
  JTextArea history;
  private boolean running;

  JMenuBar menuBar;
  JMenuItem generatePrimesMenuItem, importPrimesFileMenuItem;
  JMenu menu;

  // Network Items
  boolean serverContinue;
  ServerSocket serverSocket;

  private Vector<String> userNameList; //unique list of all users in chat room
  private Vector<ObjectOutputStream> writers;
  private Vector<Key> publicKeyList;

  // set up Server
  public Server() {
    super("Echo Server");

    publicKeyList = new Vector<Key>();

    // get content pane and set its layout
    Container container = getContentPane();
    container.setLayout(new FlowLayout());

    menuBar = new JMenuBar();
    menu = new JMenu("File");
    menuBar.add(menu);
    generatePrimesMenuItem = new JMenuItem("Generate Primes");
    importPrimesFileMenuItem = new JMenuItem("Import Primes File");
    //primesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
    //primesMenuItem.getAccessibleContext().setAccessibleDescription("Generate Primes");
    generatePrimesMenuItem.addActionListener(this);
    importPrimesFileMenuItem.addActionListener(this);
    menu.add(generatePrimesMenuItem);
    menu.add(importPrimesFileMenuItem);
    container.add(menuBar);

    // create buttons
    running = false;
    ssButton = new JButton("Start Listening");
    ssButton.addActionListener(this);
    container.add(ssButton);

    String machineAddress = null;
    try {
      InetAddress addr = InetAddress.getLocalHost();
      machineAddress = addr.getHostAddress();
    } catch (UnknownHostException e) {
      machineAddress = "127.0.0.1";
    }
    machineInfo = new JLabel(machineAddress);
    container.add(machineInfo);
    portInfo = new JLabel(" Not Listening ");
    container.add(portInfo);

    history = new JTextArea(10, 40);
    history.setEditable(false);
    container.add(new JScrollPane(history));

    setSize(500, 250);
    setVisible(true);

    userNameList = new Vector<String>();
    writers = new Vector<ObjectOutputStream>();
  } // end Server constructor

  public static void main(String args[]) {
    Server application = new Server();
    application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  //handle button event
  public void actionPerformed(ActionEvent event) {
    if (event.getSource().equals(generatePrimesMenuItem)) {
      System.out.println("generate prime code here...");
    } else if (event.getSource().equals(importPrimesFileMenuItem)) {
      System.out.println("import primes from file...");
    } else if (running == false) {
      new ConnectionThread(this);
    } else {
      serverContinue = false;
      ssButton.setText("Start Listening");
      portInfo.setText(" Not Listening ");
    }
  }

  class ConnectionThread extends Thread {
    Server server;

    public ConnectionThread(Server es3) {
      server = es3;
      start();
    }

    public void run() {
      server.serverContinue = true;

      try {
        server.serverSocket = new ServerSocket(0);
        server.portInfo.setText("Listening on Port: " + server.serverSocket.getLocalPort());
        System.out.println("Connection Socket Created");
        try {
          while (server.serverContinue) {
            System.out.println("Waiting for Connection");
            server.ssButton.setText("Stop Listening");
            new CommunicationThread(server.serverSocket.accept(), server);
          }
        } catch (IOException e) {
          System.err.println("Accept failed.");
          System.exit(1);
        }
      } catch (IOException e) {
        System.err.println("Could not listen on port: 10008.");
        System.exit(1);
      } finally {
        try {
          server.serverSocket.close();
        } catch (IOException e) {
          System.err.println("Could not close port: 10008.");
          System.exit(1);
        }
      }
    }//end run()
  }//end ConnectionThread class

  class CommunicationThread extends Thread {
    private Socket clientSocket;
    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public CommunicationThread(Socket clientSoc, Server ec3) {
      clientSocket = clientSoc;
      server = ec3;
      server.history.insert("Comminucating with Port" + clientSocket.getLocalPort() + "\n", 0);
      start();
    }

    public void run() {
      System.out.println("New Communication Thread Started");
      int numberOfUsers = 0;

      try {
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());

        try {

          Object input;
          while ((input = in.readObject()) != null) {
            //check first if the object is a string, if so, it needs to be broadcasted to all clients
            if (input.getClass().equals(String.class)) {
              String inputString = (String) input;
              if (inputString.contains("newUser: ")) {
                System.out.println("newUser Server section: ");
                history.insert(inputString + "\n", 0);
                //add username to username list and key to keylist:
                String[] elementsOfNewUserString = inputString.split(",");

                userNameList.add(elementsOfNewUserString[0].substring(9));
                numberOfUsers++;

                publicKeyList.add(
                    new Key(Long.parseLong(elementsOfNewUserString[1]), Long.parseLong(elementsOfNewUserString[2])));

                for (ObjectOutputStream writer : writers) { //tell all connected clients there's a new user
                  System.out.println("sending to a writer");
                  writer.writeObject(inputString + "\n");
                }

                for (String name : userNameList) {
                  int indexOfUserName = userNameList.indexOf(name);
                  inputString = "newUser: " + name + "," + publicKeyList.get(indexOfUserName).getX() + ","
                      + publicKeyList.get(indexOfUserName).getY();
                  out.writeObject(inputString + "\n");
                }

                writers.add(out);
                System.out.println("added new ObjectOutputStream to writers");
              } //end if new user
              else if (inputString.contains("whoIsHere")) {
                System.out.println("in whoishere");
                String userNameListToSend = new String();
                for (String userName : userNameList) {
                  userNameListToSend = userNameListToSend + userName + ", ";
                }
                out.writeObject(userNameListToSend);
              } else if (inputString.equals("Bye."))
                break;
              else if (inputString.equals("End Server."))
                server.serverContinue = false;
            } else if (input.getClass().equals(Encryption.class)) {//not a string. does not need to be broadcasted to all clients. get recipiants list and send to recipiants.
              System.out.println("if recieved encryption class");
              Encryption inputEncryption = (Encryption) input;

              for (String recipient : inputEncryption.getRecipientList()) { //send message to recipiants
                int writerIndexForUser = userNameList.indexOf(recipient);
                System.out.println("writerIndexForUser: " + writerIndexForUser);
                writers.elementAt(writerIndexForUser).writeObject(inputEncryption);
              }
            }
            //server.history.insert(inputObject + "\n", 0);
            //out.println(inputLine);

          } //end while ((inputObject = in.readObject()) != null)
        } catch (ClassNotFoundException e) {
          System.out.println("ClassNotFoundException");
        }
        out.close();
        in.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("Problem with Communication Server");
        //System.exit(1); 
      }
    }//end run()
  }//end Communication Thread
} // end class Server