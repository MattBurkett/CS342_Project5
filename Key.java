
//Sean Walker, Bilal Vijha, Matt B
//Cs 342 Program 5

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Key implements Serializable {
	private int x;
	private int y;

	public Key(Integer x, Integer y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}