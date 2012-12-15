package com.hanh.sudoku;

public interface OnCell
{
	boolean onCell(int row, int col, Cell c) throws InvalidPuzzleException;
}
