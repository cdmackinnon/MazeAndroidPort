package com.example.amazebyconnormackinnon.gui;

import java.util.Vector;

/**
 * Class stores a set of ranges, i.e., intervals. 
 * Its single current user is the FirstPersonDrawer class. 
 * 
 * Note: remove and intersect methods operate on a set of elements, 
 * however there is no method to add more than a single element to the set
 * as the set method removes all previous elements from the set. 
 * This only way to increase the cardinality is to split an existing interval
 * into two by way of the remove method.
 * 
 * This implies that ranges are disjoint and sorted in increasing order.
 *
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class RangeSet {
	
	private Vector<Interval> ranges;
	
	/**
	 * Constructor
	 */
	public RangeSet() {
		ranges = new Vector<Interval>();
	}

	/**
	 * Tells if the set is empty.
	 * @return true if the set is empty, false otherwise
	 */
	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	/**
	 * Clears the set and fills it with a single new interval as specified
	 * @param lowerBound minimum value for new element
	 * @param upperBound maximum value for new element
	 */
	public void set(int lowerBound, int upperBound) {
		ranges.removeAllElements();
		ranges.addElement(new Interval(lowerBound, upperBound));
	}

	/**
	 * Removes interval [lb,ub] from existing set
	 * such that none of its elements intersects with it anymore.
	 * Existing intervals are reduced if they intersect,
	 * split into two or fully removed if they are contained in [lb,ub]
	 * @param lowerBound is the lower bound lb of the interval
	 * @param upperBound is the upper bound ub of the interval
	 */
	public void remove(int lowerBound, int upperBound) {
		// make sure lowerBound <= upperBound
		if (upperBound < lowerBound) {
			int tmp = upperBound;
			upperBound = lowerBound;
			lowerBound = tmp;
		}
		// check all elements of the set for an overlap with interval 
		// [lowerBound, upperBound]
		// assume that set is sorted in increasing order
		for (int i = 0; i != ranges.size(); i++) {
			Interval current = (Interval) ranges.elementAt(i);
			// case 1: (current.lb <= current.ub) < (lowerBound <= upperBound)
			// current is below, so check next element as values increase
			if (current.ub < lowerBound)
				continue; 
			// case 2: (lowerBound <= upperBound) < (current.lb <= current.ub)
			// current is above, stop, no reason to check more as values only increase	
			if (current.lb > upperBound)
				return;
			// cases 3, 4, 5, 6: some overlap
			if (lowerBound <= current.lb) { 
				// case 3: (lowerBound <= (current.lb <= current.ub) <= upperBound)
				// current is inside interval
				// remove current element as it is completely covered
				// need to continue as upperBound can overlap with next element in set
				if (current.ub <= upperBound) { 
					ranges.removeElementAt(i--); // adjust index i for iterating reduced set
					continue;
				}
				// else: case 4: (lowerBound <= current.lb) <= (upperBound < current.ub)
				// current overlaps with interval, truncate current, 
				// there is no need to proceed further as upperBound does not exceed current
				// truncate intersection,  left over interval is [upperBound+1,current.ub]
				current.lb = upperBound+1;  
				return;
			}
			// control flow only gets here if previous checks fail, 
			// so current.lb < lowerBound must hold at this moment
			// case 5: current.lb <= lowerBound <= current.ub <= upperBound
			// truncate intersection,  left over interval is [current.lb,lowerBound-1]
			// need to check next element in set as upperBound may reach into it
			if (lowerBound <= current.ub && upperBound >= current.ub) { 
				current.ub = lowerBound-1; 
				continue;
			}
			// case 6: (current.lb <= upperBound) <= (lowerBound <= current.ub)
			// if all conditions fail, then [lowerBound,upperBound] lies inside 
			// the current interval
			// split current interval into two with left over ranges: 
			// [current.lb,lowerBound-1] and [upperBound+1,current.ub]
			Interval nrse = new Interval(current.lb, lowerBound-1);
			ranges.insertElementAt(nrse, i);
			// Note for correctness: we assume that intervals are ordered,
			// this is the only code that adds an element to the range set
			// lower interval goes to position i in vector, 
			// moves current interval which becomes the higher one 
			// to position i+1 in vector, which preserves the ordering
			// for current one, just update the lb to make it the higher one
			// as method returns, no need to update index i for loop progress
			current.lb = upperBound+1; 
			return;
		}
	}

	/**
	 * Computes an intersection of the given interval [lowerBound,upperBound] with the
	 * intervals in this set. If the given interval intersects with none, the method 
	 * returns null. If it intersects with at least one interval, the intersection
	 * with the first interval in this set that does so is computed and returned.
	 * For intervals, the given bounds are included. So the given bounds in
	 * the result are both elements of the intersection.
	 * @param lowerBound gives the low end of the interval of interest
	 * @param upperBound gives the high end of the interval of interest
	 * @return [lb,ub] with lower and upper bound for the intersection, null if there is none 
	 */
	public int[] getIntersection(int lowerBound, int upperBound) {
		// consider interval [lowerBound,upperBound] on x-axis
		// assume that set is sorted in increasing order
		// we look for an interval in rset that overlaps with [lowerBound,upperBound]
		for (int i = 0; i != ranges.size(); i++) {
			Interval current = (Interval) ranges.elementAt(i);
			// case 1: (current.lb <= current.ub) < (lowerBound <= upperBound)
			// current is below, so check next element as values increase			
			if (current.ub < lowerBound) 
				continue; 
			// case 2: (lowerBound <= upperBound) < (current.lb <= current.ub)
			// current is above, stop, no reason to check more as values only increase	
			if (current.lb > upperBound) 
				return null;
			// at this point: lowerBound <= current.ub and current.lb <= upperBound
			// so we have some overlap, can be at most [lb,]
			int[] result = new int[2];
			// on the low end: the bigger of the lower end of both intervals
			// on the high end: the smaller of the higher end of both intervals
			result[0] = (current.lb > lowerBound) ? current.lb : lowerBound;
			result[1] = (current.ub < upperBound) ? current.ub : upperBound;
			return result;
		}
		return null;
	}
	/**
	 * Internal class to hold a pair of two elements to represent an
	 * interval of values on the x-axis.
	 */
	class Interval {
		public int lb, ub;

		/**
		 * Constructor
		 * @param lowerBound gives the lower bound for the range
		 * @param upperBound gives the upper bound for the range
		 */
		Interval(int lowerBound, int upperBound) {
			lb = lowerBound;
			ub = upperBound;
		}
	}
}
