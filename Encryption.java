
//Sean Walker - swalke30
//Bilal Vajhi - bvajhi2
//Matt Burkett - mburke24
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
	private static final String primeFile = "primes.txt";
	private static ArrayList<Integer> primes;
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
			System.out.println("\tPublic key: (" + k.getX() + "," + k.getY() + ")");
		System.out.println("\trecipiants: " + recipiantList);
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

	private static void readPrimes(String file){
		primes = new ArrayList<Integer>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				primes.add(Integer.valueOf(line));
			}
		} catch (IOException E) {
			System.out.println("File not found: " + file);
		}
	}

	//https://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test
	private static boolean checkPrime(long n, int k){
		long r = 0, d = n-1;
		Random rng = new Random();
		if((n & 0x1) == 0)
			return false;
		while((d & 0x1) == 0){
			r++;
			d = d >> 1;
		}
		for(int i = 0; i < k; i++){
			long a = rng.nextLong() % (n-3) + 2; // [2, n-2]
			long x = powerMod(BigInteger.valueOf(a),d,n).longValue();
			if(x == 1 || x == n-1)
				continue;
			int j;
			for(j = 0; j < r-1; j++){
				x = powerMod(BigInteger.valueOf(x),2,n).longValue();
				if(x == 1)
					return false;
				if(x == n-1)
					break;
			}
			if(j >= r-1)
				return false;
		}
		return true;
	}

	public static Vector<Key> generateKeys(){
		long p = 0, q = 0;
		int i = 0;
		Random rng = new Random();
		ArrayList<Integer> local_primes;
		if (primes == null)
			readPrimes(primeFile);

		local_primes = new ArrayList<Integer>(primes);

		i = rng.nextInt(local_primes.size());
		p = local_primes.remove(i);
		i = rng.nextInt(local_primes.size());
		q = local_primes.remove(i);

		return Encryption.generateKeys(p, q);
	}

	public static Vector<Key> generateKeys(String primesFile){
		readPrimes(primesFile);
		return generateKeys();
	}

	public static Vector<Key> generateKeys(ArrayList<Integer> primes){
		Encryption.primes = primes;
		return generateKeys();
	}

	public static Vector<Key> generateKeys(long p, long q) {
		final int primeAccuracy = 20;
		long n = 0, d = 0, e = 0, phi = 0;
		int i = 0;
		Random rng = new Random();
		Vector<Key> keys = new Vector<Key>();

		if(!checkPrime(p, primeAccuracy)){
			System.out.println("not prime!: " + p);
			System.exit(-1);
		}
		if(!checkPrime(q, primeAccuracy)){
			System.out.println("not prime!: " + q);	
			System.exit(-1);		
		}

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

		Key publicKey = new Key(e, n);
		Key privateKey = new Key(d, n);
		keys.add(publicKey);
		keys.add(privateKey);

		System.out.println("Keys: (" + e + "," + n + "), (" + d + "," + n + ")");

		return keys;
	}

	public String decrypt(Key key, String name) {
		System.out.println("Decrypting!");
		System.out.println("\tPrivate key: (" + key.getX() + "," + key.getY() + ")");
		System.out.println("\tKey Owner  : " + name);

		int index = recipientList.indexOf(name);
		if(index == -1)
			return "Cannot Decrypt, " + name + " is not in the recipiant list";
		String decryptedMessage = "";
		for (BigInteger block : encryptedMessages.get(index)) {
			String decryptedBlock = "";
			block = powerMod(block, key.getX(), key.getY());
			for (int i = 0; i < Encryption.blockSize; i++) {
				decryptedBlock += (char) block.and(BigInteger.valueOf(0x7F)).intValue();
				block = block.shiftRight(7);
			}
			char reversed [] = decryptedBlock.toCharArray();
			for(int i = Encryption.blockSize - 1; i >= 0; i--)
				if(reversed[i] == '\0')
					continue;
				else
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
		Key mattPublic = keys.get(0);
		Key mattPrivate = keys.get(1);

		keys = Encryption.generateKeys();
		Key seanPublic = keys.get(0);
		Key seanPrivate = keys.get(1);

		System.out.println("");
		System.out.println("public  (Matt): " + mattPublic.getX() + ", " + mattPublic.getY());
		System.out.println("private (Matt): " + mattPrivate.getX() + ", " + mattPrivate.getY());
		System.out.println("public  (Sean): " + seanPublic.getX() + ", " + seanPublic.getY());
		System.out.println("private (Sean): " + seanPrivate.getX() + ", " + seanPrivate.getY());
		System.out.println("");

		Vector<Key> recipientKeys = new Vector<Key>();
		recipientKeys.add(mattPublic);

		Vector<String> recipientNames = new Vector<String>();
		recipientNames.add("Matt");

		String message = "hellooooooo, it's me Matt!";

		System.out.println("Encrypting message: " + message);
		Encryption secret = new Encryption(message, recipientNames, recipientKeys);
		System.out.println("Decrypted message: " + secret.decrypt(mattPrivate, "Matt"));
		System.out.println("Decrypted message: " + secret.decrypt(seanPrivate, "Matt"));

		return;
	}
}