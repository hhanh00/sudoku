package com.hanh.sudoku;

import java.util.Arrays;

public class PossibleDigits implements Cloneable
{
	public static PossibleDigits None = new PossibleDigits();
	public boolean[] digits = new boolean[9];
	
	@Override
	public PossibleDigits clone()
	{
		PossibleDigits p = new PossibleDigits();
		p.digits = Arrays.copyOf(digits, digits.length);
		return p;
	}
	
	public void clear()
	{
		for (int i = 0; i < 9; i++)
			digits[i] = true;
	}
	
	public int count()
	{
		int count = 0;
		for (int i = 0; i < 9; i++)
			if (digits[i])
				count++;
		return count;
	}
	
	public int getSingle()
	{
		for (int i = 0; i < 9; i++)
			if (digits[i])
				return i;
		return -1;
	}
}
