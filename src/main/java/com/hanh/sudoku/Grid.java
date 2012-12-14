package com.hanh.sudoku;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Grid implements Cloneable
{
	private static Logger logger = LoggerFactory.getLogger(Grid.class);
	public Cell[] cells = new Cell[9 * 9];

	public Grid()
	{
		for (int i = 0; i < 9 * 9; i++)
			cells[i] = new Cell();
	}

	public Grid clone()
	{
		Grid g = new Grid();
		for (int i = 0; i < 9 * 9; i++)
			g.cells[i] = cells[i].clone();
		return g;
	}

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

	public Cell cellAt(int row, int col)
	{
		return cells[row * 9 + col];
	}

	public void setCellAt(int row, int col, Cell c)
	{
		cells[row * 9 + col] = c;
	}

	public boolean computePenciled()
	{
		scanGrid(new OnCell()
		{
			@Override
			public boolean onCell(int row, int col, final Cell c)
			{
				if (c.digit >= 0)
					c.penciled = PossibleDigits.None;
				else
				{
					c.penciled.clear();
					scanNeighbours(row, col, new OnCell()
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

		final Pair<Void, Boolean> ret = new MutablePair<Void, Boolean>(null, false);
		scanGrid(new OnCell()
		{
			@Override
			public boolean onCell(int row, int col, Cell c)
			{
				if (c.digit < 0)
				{
					int count = c.penciled.count();
					if (count == 0)
						ret.setValue(true);

					if (count == 1)
					{
						c.digit = c.penciled.getSingle();
						ret.setValue(true);
					}
				}
				return true;
			}
		});
		return ret.getValue();
	}

	public boolean checkValid()
	{
		return scanGrid(new OnCell()
		{
			@Override
			public boolean onCell(int row, int col, final Cell c1)
			{
				if (c1.digit < 0)
				{
					return c1.penciled.count() != 0;
				}

				return scanNeighbours(row, col, new OnCell()
				{
					@Override
					public boolean onCell(int row, int col, Cell c2)
					{
						if (c1 == c2)
							return true;

						if (c2.digit < 0)
							return true;

						return c1.digit != c2.digit;
					}
				});
			}
		});
	}

	public boolean scanGrid(OnCell cb)
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

	private boolean scanNeighbours(int row, int col, OnCell cb)
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

	public boolean checkComplete()
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

	public static void main(String[] args)
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
		*/
		// World's hardest?
		g.set("8        " 
		    + "  36     " 
			+ " 7  9 2  " 
		    + " 5   7   " 
			+ "    457  " 
		    + "   1   3 " 
			+ "  1    68" 
		    + "  85   1 " 
			+ " 9    4  ");
		
		// @formatter:on

		g.computePenciled();
		logger.info("\n{}", g);
		Solver s = new Solver();
		s.solve(g, 0);
	}
}
