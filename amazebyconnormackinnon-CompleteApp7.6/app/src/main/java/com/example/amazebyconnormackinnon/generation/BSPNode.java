/**
 * 
 */
package com.example.amazebyconnormackinnon.generation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.example.amazebyconnormackinnon.gui.MazeFileWriter;

/**
 * BSPNodes are used to build a binary tree, where internal nodes keep track 
 * of lower and upper bounds of (x,y) coordinates.
 * Leaf nodes carry a list of walls. 
 * A BSP tree is a data structure to search for a set of walls to put on
 * display in the FirstPersonView and the Map.
 * 
 * Superclass for BSPBranch and BSPLeaf nodes that carry further data. 
 * 
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 *  
 */
public class BSPNode {
	/* lower and upper bounds for (x,y) coordinates of walls stored in leaf nodes. */
	private int lowX;
	private int lowY;
	private int highX;
	private int highY;    

	/**
	 * Store the content of a BSPNode including data of branches and leaves as special cases.
	 * @param doc document to add data to
	 * @param mazeXML element to add data to
	 * @param number is an index number for this node in the XML format
	 * @return the highest used index number, in this case the given number
	 */
	public int store(Document doc, Element mazeXML, int number) {
		// xlBSPNode elements
		MazeFileWriter.appendChild(doc, mazeXML, "xlBSPNode_" + number, lowX) ;
		// ylBSPNode elements
		MazeFileWriter.appendChild(doc, mazeXML, "ylBSPNode_" + number, lowY) ;
		// xuBSPNode elements
		MazeFileWriter.appendChild(doc, mazeXML, "xuBSPNode_" + number, highX) ;
		// yuBSPNode elements
		MazeFileWriter.appendChild(doc, mazeXML, "yuBSPNode_" + number, highY) ;
		// isleafBSPNode elements
		MazeFileWriter.appendChild(doc, mazeXML, "isleafBSPNode_" + number, isIsleaf()) ;

		return number ; // unchanged
	}

	/**
	 * @return tells if object is a leaf node
	 */
	public boolean isIsleaf() {
		return false ;
	}

	/**
	 * Updates internal fields for upper and lower bounds of (x,y) coordinates.
	 * Given parameter values are taken for bounds if they exceed what is already
	 * in store.
	 * @param x used to update xl and xu
	 * @param y used to update yl and yu
	 */
	protected void updateBounds(int x, int y) {
		// update fields in BSPNode
		// method is only used in subclass BSPLeafNode
		lowX = Math.min(lowX, x);
		lowY = Math.min(lowY, y);
		highX = Math.max(highX, x);
		highY = Math.max(highY, y);

	}

	/**
	 * @return the lower bound for x
	 */
	public int getLowerBoundX() {
		return lowX;
	}

	/**
	 * @param xl the lower bound for x to set
	 */
	public void setLowerBoundX(int xl) {
		this.lowX = xl;
	}

	/**
	 * @return the lower bound for y
	 */
	public int getLowerBoundY() {
		return lowY;
	}

	/**
	 * @param yl the lower bound for y to set
	 */
	public void setLowerBoundY(int yl) {
		this.lowY = yl;
	}

	/**
	 * @return the upper bound for x
	 */
	public int getUpperBoundX() {
		return highX;
	}

	/**
	 * @param xu the upper bound for x to set
	 */
	public void setUpperBoundX(int xu) {
		this.highX = xu;
	}

	/**
	 * @return the upper bound for u
	 */
	public int getUpperBoundY() {
		return highY;
	}

	/**
	 * @param yu the upper bound for y to set
	 */
	public void setUpperBoundY(int yu) {
		this.highY = yu;
	}
}