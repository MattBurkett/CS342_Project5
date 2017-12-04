
//Sean Walker, Bilal Vijha, Matt B
//Cs 342 Program 5

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Key implements Serializable {
	private long x; // d or e
	private long y; // n

	public Key(long x, long y) {
		this.x = x;
		this.y = y;
	}

	public long getX() {
		return x;
	}

	public long getY() {
		return y;
	}
}