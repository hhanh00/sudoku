package com.hanh.sudoku;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solver
{
	private static Logger logger = LoggerFactory.getLogger(Solver.class);
	
	private int nBranchesVisited = 1;
	
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
			if (!g.checkValid())
			{
				logger.info("Puzzle invalid");
				return false;
			}
			
			if (!g.computePenciled())
			{
				FindCellWithLowestPencilCount onCell = new FindCellWithLowestPencilCount();
				g.scanGrid(onCell);
				logger.info("{}> Branching on cell {}/{}", new Object[] { depth, onCell.minRow + 1, onCell.minCol + 1});
				for (int i = 0; i < 9; i++)
				{
					Cell c = g.cellAt(onCell.minRow, onCell.minCol);
					if (c.penciled.digits[i])
					{
						logger.info("Option {}", i + 1);
						Grid g2 = g.clone();
						g2.cellAt(onCell.minRow, onCell.minCol).digit = i;
						boolean solved = solve(g2, depth + 1);
						if (solved)
							return true;
					}
				}
				nBranchesVisited++;
				logger.info("{}> Backtrack", depth);
				return false;
			}
		}
	}
}
