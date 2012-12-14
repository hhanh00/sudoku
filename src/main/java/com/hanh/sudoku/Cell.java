package com.hanh.sudoku;

public class Cell implements Cloneable
{
	public PossibleDigits penciled = new PossibleDigits();
	public int digit = -1;
	
	@Override
	public Cell clone()
	{
		Cell c = new Cell();
		c.digit = digit;
		c.penciled = penciled.clone();
		return c;
	}
}
