//Sean Walker - swalke30
//Bilal V - 
//CS 342 Program 5 - Networked Chat with RSA Encryption/Decryption

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
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

  // Network Items
  boolean serverContinue;
  ServerSocket serverSocket;

  private Vector<String> userNameList; //unique list of all users in chat room
  private HashSet<PrintWriter> writers;

  // set up Server
  public Server() {
    super("Echo Server");

    // get content pane and set its layout
    Container container = getContentPane();
    container.setLayout(new FlowLayout());

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
    writers = new HashSet<PrintWriter>();
  } // end Server constructor

  public static void main(String args[]) {
    Server application = new Server();
    application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  //handle button event
  public void actionPerformed(ActionEvent event) {
    if (running == false) {
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
    //private boolean serverContinue = true;
    private Socket clientSocket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;

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
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
          server.history.insert(inputLine + "\n", 0);
          //out.println(inputLine);

          if (inputLine.contains("newUser: ")) {
            userNameList.add(numberOfUsers, inputLine.substring(9));
            numberOfUsers++;
            

            for (PrintWriter writer : writers) { //tell all connected clients there's a new user
              System.out.println("sending to a writer");
              writer.println(inputLine + "\n");
            }

            writers.add(out);
          }
          
          else if (inputLine.contains("whoIsHere")) {
            for (String userName : userNameList) {
              out.println(userName);
            }
          }
          else if (inputLine.equals("Bye."))
            break;

          else if (inputLine.equals("End Server."))
            server.serverContinue = false;

          else
            for (PrintWriter writer : writers) { //send message to all connected clients
              writer.println(inputLine);
            }
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