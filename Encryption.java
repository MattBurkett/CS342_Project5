
//Sean Walker - swalke30
//Bilal V - 
//Matt B
//CS 342 Program 5 - Networked Chat with RSA Encryption/Decryption

import java.net.*;
import java.lang.*;
import java.math.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Encryption implements Serializable {
	private static final Integer primes[] = { 3666233, 5170637, 4456451, 6830029, 3783541, 5021531, 3345457, 1123379,
			4125497, 3643411, 5006543, 4403053, 2449729, 8084411, 3126083, 9657247, 3908467, 7970257, 3715783, 4562693,
			4694999, 6087707, 5514469, 3317177, 5955329, 5624743, 5216777, 6413153, 7701923, 9315689, 6819931, 5643851,
			4491329, 2618107, 7215601, 2387249, 8100133, 8860601, 8322403, 8199913, 3286301, 5581591, 3994619, 3480073,
			3733649, 9082691, 9290233, 6748459, 6070907, 2873417 };
	private static final int blockSize = 4;
	private Vector<String> recipientList; //who is the message for
	private Vector<Vector<BigInteger>> encryptedMessages;

	public Encryption(String message, Vector<String> recipiantList, Vector<Key> publicKeys) {
		Encrypt(message, recipiantList, publicKeys);
	}

	public Encryption(String message, Vector<String> recipiantList, Key publicKey) {
		Vector<Key> publicKeys = new Vector<Key>();
		publicKeys.add(publicKey);
		Encrypt(message, recipiantList, publicKeys);
	}

	public void Encrypt(String message, Vector<String> recipiantList, Vector<Key> publicKeys) {
		System.out.println("Encrypting!");
		for (Key k : publicKeys)
			System.out.println("key: (" + k.getX() + "," + k.getY() + ")");
		System.out.println("recipiants: " + recipiantList);
		this.recipientList = recipiantList;
		this.encryptedMessages = new Vector<Vector<BigInteger>>();

		//do encryption here...
		char raw[] = message.toCharArray();

		for (Key publicKey : publicKeys) {
			Vector<BigInteger> encryptedMessage = new Vector<BigInteger>();
			long e = publicKey.getX();
			long n = publicKey.getY();
			int i = 0;
			while (true) {
				BigInteger block = BigInteger.valueOf(0);
				for (int j = i; j < i + Encryption.blockSize; j++) {
					if (raw.length == j)
						break;
					block = block.shiftLeft(7); // *128
					block = block.add(BigInteger.valueOf(raw[j]));
				}
				i += Encryption.blockSize;
				encryptedMessage.add(powerMod(block, publicKey.getX(), publicKey.getY()));
				if (i > raw.length)
					break;
			}
			encryptedMessages.add(encryptedMessage);
		}
	}

	public static Vector<Key> generateKeys() {
		long p = 0, q = 0;
		int i = 0;
		Random rng = new Random();
		ArrayList<Integer> local_primes = new ArrayList<Integer>(Arrays.asList(Encryption.primes));

		i = rng.nextInt(local_primes.size());
		p = local_primes.remove(i);
		i = rng.nextInt(local_primes.size());
		q = local_primes.remove(i);

		return Encryption.generateKeys(p, q);
	}

	public static Vector<Key> generateKeys(long p, long q) {
		long n = 0, d = 0, e = 0, phi = 0;
		int i = 0;
		Random rng = new Random();
		Vector<Key> keys = new Vector<Key>();

		n = p * q;
		phi = (p - 1) * (q - 1);

		long maxE = 5000;
		double growthRate = 1.1;
		do {
			e = rng.nextLong() % maxE;
			maxE = (long) (maxE >= n ? n : maxE * growthRate);
		} while (Encryption.GCD(e, phi) != 1);

		d = Encryption.MIV(e, phi);

		BigInteger Be = BigInteger.valueOf(e);
		BigInteger Bd = BigInteger.valueOf(d);
		BigInteger Bphi = BigInteger.valueOf(phi);

		//System.out.println("Phi: " + phi);
		//System.out.println("e*d mod Phi: " + ( Be.multiply(Bd) ).mod(Bphi) );

		Key publicKey = new Key(e, n);
		Key privateKey = new Key(d, n);
		keys.add(publicKey);
		keys.add(privateKey);

		System.out.println("Keys: (" + e + "," + n + "), (" + d + "," + n + ")");

		return keys;
	}

	public String decrypt(Key key, String name) {
		System.out.println("Decrypting!");
		System.out.println("key: (" + key.getX() + "," + key.getY() + ")");
		System.out.println("recipiant: " + name);

		int index = recipientList.indexOf(name);
		String decryptedMessage = "";
		for (BigInteger block : encryptedMessages.get(index)) {
			String decryptedBlock = "";
			block = powerMod(block, key.getX(), key.getY());
			for (int i = 0; i < Encryption.blockSize; i++) {
				decryptedBlock += (char) block.and(BigInteger.valueOf(0x7F)).intValue();
				block = block.shiftRight(7);
			}
			char reversed[] = decryptedBlock.toCharArray();
			for (int i = Encryption.blockSize - 1; i >= 0; i--)
				decryptedMessage += reversed[i];

		}
		return decryptedMessage;
	}

	public Vector<String> getRecipientList() {
		return recipientList;
	}

	public String getMessage() { //for testingpurposes, matt will delete this
		return "DID NOT DECRYPT! USE `decrypt()`";
	}

	private static BigInteger powerMod(BigInteger base, long e, long m) {
		BigInteger exponent = BigInteger.valueOf(e);
		BigInteger modulus = BigInteger.valueOf(m);
		BigInteger result = BigInteger.valueOf(1);
		while (exponent.compareTo(BigInteger.valueOf(0)) > 0) {
			if (exponent.mod(BigInteger.valueOf(2)).compareTo(BigInteger.valueOf(1)) == 0)
				result = result.multiply(base).mod(modulus);
			exponent = exponent.shiftRight(1);
			base = base.multiply(base).mod(modulus);
		}
		return result;
	}

	// Eculidean algorithm
	private static long GCD(long a, long b) {
		Vector<Long> r = new Vector<Long>();
		r.add(a > b ? a : b);
		r.add(a > b ? b : a);

		while (r.lastElement() > 0)
			r.add(r.get(r.size() - 2) % r.lastElement());

		return r.get(r.size() - 2);
	}

	// Extended Eculidean algorithm
	// https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm#Computing_multiplicative_inverses_in_modular_structures
	private static long MIV(long a, long n) {
		long q, prevr, prevt;
		long t = 0, r = n, newt = 1, newr = a;

		while (newr != 0) {
			q = r / newr;

			prevt = t;
			t = newt;
			newt = prevt - q * newt;

			prevr = r;
			r = newr;
			newr = prevr - q * newr;
		}
		if (r > 1) {
			System.out.println("a is not invertible");
			System.exit(0);
		}
		if (t < 0)
			t += n;

		return t;
	}

	//used for testing, should execute client or server
	public static void main(String[] args) {
		Vector<Key> keys = Encryption.generateKeys();
		Key Public = keys.get(0);
		Key Private = keys.get(1);

		System.out.println("public : " + Public.getX() + ", " + Public.getY());
		System.out.println("private: " + Private.getX() + ", " + Private.getY());

		Vector<Key> recipientKeys = new Vector<Key>();
		recipientKeys.add(Public);

		Vector<String> recipientNames = new Vector<String>();
		recipientNames.add("Matt");

		String message = "hellooooooo, it's me Matt!";

		System.out.println("Encrypting message: " + message);
		Encryption secret = new Encryption(message, recipientNames, recipientKeys);
		System.out.println("Decrypted message: " + secret.decrypt(Private, "Matt"));

		return;
	}
}