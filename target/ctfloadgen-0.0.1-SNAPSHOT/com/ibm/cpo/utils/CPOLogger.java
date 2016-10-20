package com.ibm.cpo.utils;

public class CPOLogger 
{
	private static final CPOLogger INSTANCE = new CPOLogger();
	private boolean _debugIndicator = false;				// default, no debug messages!
	
	// allows debug indicator to be turned on or off at runtime.
	// You can change debug print behavior dynamically if you wish.
	// default is debug turned off
	public static void setDebugIndicator(boolean debugInd)
	{
		CPOLogger.getInstance()._setDebugIndicator(debugInd);
	}
	
	public static void printDebug( String msg )
	{
		CPOLogger.getInstance()._printDebug(msg);
	}
	
	public static void printError( String msg )
	{
		// print error to standard out and error out
		System.out.println("CPO ERROR: " + msg);
		System.err.println("CPO ERROR: " + msg);
	}
	
//------------------------------------------------------------------------------	
// private methods here down	
//------------------------------------------------------------------------------	
	
	/* Here we are not creating Singleton instance inside getInstance() method 
	 * instead it will be created by ClassLoader. 
	 * Also private constructor makes impossible to create another instance , except one case. 
	 * You can still access private constructor by reflection and calling setAccessible(true). 
	 * By the way You can still prevent creating another instance of Singleton by this way by 
	 * throwing Exception from constructor.
	 * Read more: 
	 * http://javarevisited.blogspot.com/2012/12/how-to-create-thread-safe-singleton-in-java-example.html#ixzz34KzagVKf
	 */
	private static CPOLogger getInstance()
	{
        return INSTANCE;
    }
	
	private CPOLogger()
	{
		// Do NOT allow constructor to be called more than once!
		if( INSTANCE != null )
		{	
			java.lang.RuntimeException e = new java.lang.RuntimeException
				("FLoadEngine() consructor should never be called! This is a singleton instance!");
			
			throw(e);
		}	
	}

	private void _setDebugIndicator(boolean debugInd)
	{
		_debugIndicator = debugInd;
	}

	private void _printDebug( String msg )
	{
		if( _debugIndicator == true )
		{
			System.out.println("CPO DEBUG: " + msg);
		}
	}
}
