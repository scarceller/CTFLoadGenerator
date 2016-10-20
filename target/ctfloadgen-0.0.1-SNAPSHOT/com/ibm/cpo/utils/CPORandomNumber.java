package com.ibm.cpo.utils;

import java.util.Random;

// super fast random number generator!
public class CPORandomNumber 
{
	long _seed = System.currentTimeMillis();
	
	// test harness	
	public static void main(String[] args) 
	{
		CPORandomNumber rGen1 = new CPORandomNumber();
		Random rGen2 = new Random();

		long startTime = 0;
		long endTime = 0;
		long random = 0;
				
		// test CPO random generator
		startTime = System.currentTimeMillis();
		for(long ii=1; ii<=100000000; ii++)
		{	
			random = rGen1.nextLong();
//			random = (random % 5)+1;	// convert to 1-5
//			System.out.println(random);
		}
		endTime = System.currentTimeMillis();
		System.out.println("CPO random execution time = " + (endTime - startTime) );
		
		// test java.util.Random generator
		startTime = System.currentTimeMillis();
		for(long ii=1; ii<=100000000; ii++)
		{	
			random = rGen2.nextLong();
//			random = (random % 5)+1;	// convert to 1-5
//			System.out.println(random);
		}
		endTime = System.currentTimeMillis();
		System.out.println("SYS random execution time = " + (endTime - startTime) );
	}
	
	/*
	This method produces what we might describe as "medium quality" random numbers: 
	certainly better than the LCG algorithm used by java.util.Random. 
	(In particular, any bits of the resulting numbers can be assumed to be equally random. 
	Contrast this with the randomness of LCG generators such as java.lang.Random, 
	where how random a bit is depends on its position.) 
	And what is powerful is that it does so using low-cost operations: 
	shifts and XORs, with only a single word of state (seed in the code below). 
	In the light of this method, it's probably fair to say that LCGs used in isolation are a 
	little bit of a waste of space. If the XORShift method had been discovered when java.lang.Random 
	was first developed, the Java library authors may well have chosen it over LCG3.

	The "magic" values of 21, 35 and 4 have been found to produce good results. 
	With these values, the generator has a full period of 264-1, and the resulting values pass 
	Marsaglia's "Diehard battery" of statistical tests for randomness4. L'Ecuyer & Simard (2007)5 also 
	found that values of 13, 7 and 17 fail on only 7 of the 160 tests of their BigCrush battery 
	(for comparison, java.util.Random fails on 21, while crypographic generators such as Java's 
	SecureRandom— as indeed we might expect— generally pass all tests).
	
	Source: http://www.javamex.com/tutorials/random_numbers/xorshift.shtml#.U6HWuBApX0o
	*/ 
	public long nextLong()
	{
		long seed = this._seed;	// generate the seed from system time
		
		// create the random number using the seed
		seed ^= (seed << 21);			// signed shift left 21 times  
		seed ^= (seed >>> 35);			// unsigned shift right 35 times
		seed ^= (seed << 4);			// signed shift left 4 times
		
		seed ^= (seed >> 1);			// insure no negative numbers by right shift a zero into MSB
				
		this._seed = seed;				// now set seed to this random
		
		return(seed);	
	}
}
