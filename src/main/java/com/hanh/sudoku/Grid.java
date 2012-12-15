package com.hanh.sudoku;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Soduku grid
 * @author hanh
 */
public class Grid implements Cloneable
{
	private static Logger logger = LoggerFactory.getLogger(Grid.class);
	private Cell[] cells = new Cell[9 * 9];

	/**
	 * Constructor for a blank grid
	 */
	public Grid()
	{
		for (int i = 0; i < 9 * 9; i++)
			cells[i] = new Cell();
	}

	/**
	 * Make a deep copy
	 */
	public Grid clone()
	{
		Grid g = new Grid();
		for (int i = 0; i < 9 * 9; i++)
			g.cells[i] = cells[i].clone();
		return g;
	}

	/**
	 * Set the grid based on the string of 81 characters that represent the 9x9 grid
	 * @param grid
	 */
	public void set(String grid)
	{
		for (int i = 0; i < 9 * 9; i++)
		{
			char c = grid.charAt(i);
			if (c != ' ')
			{
				int v = Integer.parseInt(new String(new char[]
				{ c })) - 1;
				cellAt(i / 9, i % 9).digit = v;
			}
		}
	}

	/**
	 * Return the cell at position row, col
	 * @param row index from 0 to 8
	 * @param col index from 0 to 8
	 * @return cell
	 */
	public Cell cellAt(int row, int col)
	{
		return cells[row * 9 + col];
	}

	/**
	 * Compute the penciled digits for each cell of the grid
	 * Then if there are cells with only one penciled digit, fill the cell with that digit 
	 * @return true if at least a cell was filled in
	 * @throws InvalidPuzzleException if there are cells with no possible digit or if after
	 * setting the unique digits, the puzzle has repeated digits
	 */
	public boolean computePenciled() throws InvalidPuzzleException
	{
		// pencil every cell in the grid
		scanGrid(new OnCell()
		{
			@Override
			public boolean onCell(int row, int col, final Cell c) throws InvalidPuzzleException
			{
				// skip the cells that have a digit
				if (c.digit >= 0)
					c.penciled = PossibleDigits.None;
				else
				{
					// scan all the neighbours and pencil in the remaining digits
					c.penciled.clear();
					scanNeighbors(row, col, new OnCell()
					{
						@Override
						public boolean onCell(int row, int col, Cell scanCell)
						{
							if (scanCell.digit >= 0)
								c.penciled.digits[scanCell.digit] = false;
							return true;
						}
					});
				}
				return true;
			}
		});

		// output value, java won't let the inner function modify a local variable directly
		final Pair<Void, Boolean> ret = new MutablePair<Void, Boolean>(null, false);
		scanGrid(new OnCell()
		{
			@Override
			public boolean onCell(int row, int col, Cell c) throws InvalidPuzzleException
			{
				// blank cell...
				if (c.digit < 0)
				{
					int count = c.penciled.count();
					// no possible digit left, it is not a valid puzzle
					if (count == 0)
						throw new InvalidPuzzleException();

					// only one possible digit left, fill the cell with it
					if (count == 1)
					{
						c.digit = c.penciled.getSingle();
						ret.setValue(true);
					}
				}
				return true;
			}
		});
		
		// Now check that the puzzle is still valid, it may not be
		// if we fill in twice the same digit in the same row, col, or sector
		if (ret.getValue())
			checkValid();
		return ret.getValue();
	}

	/**
	 * Invoke the cb delegate on every cell of the grid. Stop if the cb returns false
	 * @param cb
	 * @return true if every cell was visited, false if cb returned false and the scan was
	 * aborted
	 * @throws InvalidPuzzleException
	 */
	public boolean scanGrid(OnCell cb) throws InvalidPuzzleException
	{
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
			{
				final Cell c = cellAt(row, col);
				if (!cb.onCell(row, col, c))
					return false;
			}
		return true;
	}

	/**
	 * Invoke the cb delegate on every neighbor cell. A cell is a neighbor if
	 * it is in the same row, column or sector. A cell is a neighbor of itself
	 * @param row
	 * @param col
	 * @param cb
	 * @return
	 * @throws InvalidPuzzleException
	 */
	private boolean scanNeighbors(int row, int col, OnCell cb) throws InvalidPuzzleException
	{
		for (int i = 0; i < 9; i++)
		{
			if (!cb.onCell(row, i, cellAt(row, i)))
				return false;
			if (!cb.onCell(i, col, cellAt(i, col)))
				return false;
			int srow = row / 3;
			int scol = col / 3;
			if (!cb.onCell(srow * 3 + i / 3, scol * 3 + i % 3, cellAt(srow * 3 + i / 3, scol * 3 + i % 3)))
				return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 27; i++)
		{
			int row = i / 3;
			for (int j = 0; j < 27; j++)
			{
				int col = j / 3;
				Cell c = cellAt(row, col);
				int subRow = i % 3;
				int subCol = j % 3;
				int penciledDigit = subRow * 3 + subCol;
				if (c.digit >= 0)
				{
					if (penciledDigit != 4)
						sb.append('*');
					else
						sb.append(c.digit + 1);
				}
				else
				{
					sb.append(c.penciled.digits[penciledDigit] ? penciledDigit + 1 : " ");
				}
			}
			sb.append('\n');
		}

		return sb.toString();
	}

	/**
	 * Check if the puzzle is completely filled
	 * @return
	 */
	public boolean checkComplete()
	{
		try
		{
			return scanGrid(new OnCell()
			{
				@Override
				public boolean onCell(int row, int col, Cell c)
				{
					return c.digit >= 0;
				}
			});
		}
		catch (InvalidPuzzleException e)
		{
		}
		return false;
	}

	public static void main(String[] args) throws InvalidPuzzleException
	{
		BasicConfigurator.configure();
		Grid g = new Grid();

		// @formatter:off
		/* Easy 
		g.set(" 7 4  5  " 
		    + " 1 32  84" 
			+ "9   61   " 
		    + "  5 8  29" 
			+ " 297 516 " 
		    + "68  4 7  " 
			+ "   81   7" 
		    + "86  54 3 " 
			+ "  2  3 4 ");
		*/
		/* Medium
		g.set("2  8     " 
		    + "7 3 2  84" 
			+ " 89 61   " 
		    + "  81 9  3" 
			+ "         " 
		    + "5  3 61  " 
			+ "   69 41 " 
		    + "14  5 3 8" 
			+ "     3  5");
		*/
		/* Hard
		g.set("  3    78" 
		    + "   2  3  " 
			+ "   834 6 " 
		    + "7    94 1" 
			+ "         " 
		    + "5 21    9" 
			+ " 9 378   " 
		    + "  6  5   " 
			+ "48    5  ");
		*/
		/* Extreme
		g.set("72    6  " 
		    + "       37" 
			+ "  39   2 " 
		    + "   2598  " 
			+ "    7    " 
		    + "  9468   " 
			+ " 8   57  " 
		    + "56       " 
			+ "  2    95");
		g.set("  7  5   " 
		    + " 8   6   " 
			+ "5 621   4" 
		    + "1        " 
			+ " 6 1 7 8 " 
		    + "        3" 
			+ "9   324 5" 
		    + "   4   2 " 
			+ "   9  3  ");
		g.set("8        " 
		    + "  36     " 
			+ " 7  9 2  " 
		    + " 5   7   " 
			+ "    457  " 
		    + "   1   3 " 
			+ "  1    68" 
		    + "  85   1 " 
			+ " 9    4  ");
		*/
		g.set("   4   2 " 
		    + "9    3  5" 
			+ " 4   76  " 
		    + "2   38   " 
			+ " 5     1 " 
		    + "   91   6" 
			+ "  68   9 " 
		    + "7  3    2" 
			+ " 1   5   ");
		/*
		g.set("     6   " 
		    + " 59     8" 
			+ "2    8   " 
		    + " 45      " 
			+ "  3      " 
		    + "  6  3 54" 
			+ "   324  6" 
		    + "         " 
			+ "         ");
		*/
		// @formatter:on

		g.computePenciled();
		logger.info("\n{}", g);
		Solver s = new Solver();
		s.solve(g, 0);
	}

	public void checkValid() throws InvalidPuzzleException
	{
		scanGrid(new OnCell()
		{
			@Override
			public boolean onCell(int row, int col, final Cell c1) throws InvalidPuzzleException
			{
				if (c1.digit < 0)
				{
					return c1.penciled.count() != 0;
				}

				return scanNeighbors(row, col, new OnCell()
				{
					@Override
					public boolean onCell(int row, int col, Cell c2) throws InvalidPuzzleException
					{
						if (c1 == c2)
							return true;

						if (c2.digit < 0)
							return true;

						if (c1.digit == c2.digit)
							throw new InvalidPuzzleException();
						return true;
					}
				});
			}
		});
	}
}
