package com.hanh.sudoku;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solver
{
	private static Logger logger = LoggerFactory.getLogger(Solver.class);
	
	private int nBranchesVisited = 1;
	
	/*
	 * Find the cell that has the lower number of penciled digits
	 */
	private static class FindCellWithLowestPencilCount implements OnCell
	{
		private int minRow = -1;
		private int minCol = -1;
		private int minCount = 10;

		@Override
		public boolean onCell(int row, int col, Cell c)
		{
			int count = c.penciled.count();
			if (count > 0 && count < minCount)
			{
				minCount = count;
				minRow = row;
				minCol = col;
			}
			return true;
		}
	}

	/**
	 * Solve a sudoku puzzle
	 * @param g grid
	 * @param depth the level of recursion (for display purposes only)
	 * @return true if the puzzle was solved
	 */
	public boolean solve(Grid g, int depth)
	{
		while (true)
		{
			if (g.checkComplete())
			{
				logger.info("Puzzle solved");
				logger.info("\n{}", g);
				logger.info("{} branches visited", nBranchesVisited);
				return true;
			}
			
			try
			{
				/* Compute the penciled digits
				 * If the function returns true, at least one cell was filled and just continue
				 * because we made some progress
				 */
				if (!g.computePenciled())
				{
					/*
					 * We can't decide which digit to choose for sure. We have to make a guess.
					 * Find the cell with the lowest number of options (penciled digits) and
					 * choose each one in turn
					 */
					FindCellWithLowestPencilCount onCell = new FindCellWithLowestPencilCount();
					g.scanGrid(onCell);
					logger.info("{}> Branching on cell {}/{}", new Object[] { depth, onCell.minRow + 1, onCell.minCol + 1});
					for (int i = 0; i < 9; i++)
					{
						Cell c = g.cellAt(onCell.minRow, onCell.minCol);
						if (c.penciled.digits[i])
						{
							/*
							 * Choose that option. Make a copy of the grid because we are going to try it out 
							 * and there is no guarantee that it will be the right choice. We may end up
							 * backtracking and we should leave the grid untouched if we have to resume 
							 */
							logger.info("Option {}", i + 1);
							Grid g2 = g.clone();
							// Fill that cell with that option
							g2.cellAt(onCell.minRow, onCell.minCol).digit = i;
							// Try to solve that new grid
							boolean solved = solve(g2, depth + 1);
							// If we were successful, that was the right choice
							if (solved)
								return true;
							// If not, try with the next option
						}
					}
					// All the options were wrong, this is a dead end
					nBranchesVisited++;
					logger.info("{}> Backtrack", depth);
					return false;
				}
			}
			catch (InvalidPuzzleException e)
			{
				return false;
			}
		}
	}
}
