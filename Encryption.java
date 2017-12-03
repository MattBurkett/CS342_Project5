
//Sean Walker - swalke30
//Bilal V - 
//Matt B
//CS 342 Program 5 - Networked Chat with RSA Encryption/Decryption

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Encryption implements Serializable {
	private Vector<String> recipientList; //who is the message for
	private String message;

	public Encryption(String message, Vector<String> recipiantList, Key publicKey) {
		this.message = message;
		this.recipientList = recipiantList;
		//do encryption here...
		int e = publicKey.getX();
		int n = publicKey.getY();

	}

	public static Vector<Key> generateKeys() {
		Vector<Key> keys = new Vector<Key>();
		Key publicKey = new Key(3, 5);
		Key privateKey = new Key(7, 11);
		keys.add(publicKey);
		keys.add(privateKey);
		return keys;
	}

	public String decrypt(int key, String name) {
		return message;
	}

	public Vector<String> getRecipientList() {
		return recipientList;
	}

	public String getMessage() { //for testingpurposes, matt will delete this
		return message;
	}

}