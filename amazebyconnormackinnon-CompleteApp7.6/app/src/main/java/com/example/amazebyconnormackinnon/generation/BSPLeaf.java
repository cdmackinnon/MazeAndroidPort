/**
 * 
 */
package com.example.amazebyconnormackinnon.generation;

import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.example.amazebyconnormackinnon.gui.MazeFileWriter;

/**
 * A leaf node for a tree of BSPNodes. It carries a list of walls. 
 * 
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class BSPLeaf extends BSPNode {
	/**
	 * The logger is used to track execution and report issues.
	 */
	private static final Logger LOGGER = Logger.getLogger(BSPLeaf.class.getName());


	private List<Wall> allWalls; // list of walls

	/**
	 * Constructor
	 * stores reference to given list of walls and updates bounds
	 * @param walls is a list of walls, can not be empty
	 */
	public BSPLeaf(List<Wall> walls) {
		// list should not be empty as this is the only way to provide content
		assert (!walls.isEmpty()) : "BSPLeaf needs walls, list is empty!" ;
		// need to memorize walls 
		allWalls = walls;
		// update the bounds that are kept in the super class
		updateBounds();
	}

	/**
	 * Update bounds based on min and max values seen in start and end positions
	 * in the internal list of walls
	 */
	private void updateBounds() {
	    setLowerBoundX(Integer.MAX_VALUE); 
        setUpperBoundX(Integer.MIN_VALUE);
        setLowerBoundY(Integer.MAX_VALUE); 
        setUpperBoundY(Integer.MIN_VALUE); 
        for (Wall wall: allWalls) {
            updateBounds(wall.getStartPositionX(), wall.getStartPositionY());
            updateBounds(wall.getEndPositionX(), wall.getEndPositionY());
        }
	}
	/**
	 * @return tells if object is a leaf node
	 */
	@Override
	public boolean isIsleaf() {
		return true ;
	}
	/**
	 * Store the content of a leaf node, in particular its list of walls.
	 * All entries carry the number of the node as an index and each wall has an additional second index for the wall number.
	 * @param doc document to add data to
	 * @param mazeXML element to add data to
	 * @param number is an index number for this node in the XML format
	 * @return the highest used index number, in this case the given number
	 */
	@Override
	public int store(Document doc, Element mazeXML, int number) {
		super.store(doc, mazeXML, number) ; //leaves number unchanged
		if (!isIsleaf())
			LOGGER.warning("Node does not carry isleaf flag but is a BSPLeaf, this is inconsistent!");
		// store list of walls, store total number of elements first
		MazeFileWriter.appendChild(doc, mazeXML, "numSeg_" + number, allWalls.size()) ;
		int i = 0 ;
		for (Wall wall : allWalls)
		{
			wall.storeWall(doc, mazeXML, number, i);
			i++ ;
		}
		return number ;
	}

	/**
	 * @return the list of walls 
	 */
	public List<Wall> getAllWalls() {
		return allWalls;
	}

}

