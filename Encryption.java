//Sean Walker - swalke30
//Bilal V - 
//Matt
//CS 342 Program 5 - Networked Chat with RSA Encryption/Decryption

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Encryption {
	private Vector<String> recipientList; //who is the message for
	private String message;

	public Encryption(String message, Vector<String> recipiantList, Vector<Integer> key){
		this.message = message;		
		this.recipientList = recipiantList;		
		//do encryption here......
	}
	
	public static Vector<Integer> generateKeys(){
		return null;
	}

}