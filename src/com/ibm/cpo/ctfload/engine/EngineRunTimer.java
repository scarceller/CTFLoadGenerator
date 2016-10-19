package com.ibm.cpo.ctfload.engine;

public class EngineRunTimer extends Thread 
{
	private CTFLoadEngine _engine = null;		// Reference to the engine instance
	private long _runTime = 0;				// Timer's length of time to wait
	
	public EngineRunTimer(CTFLoadEngine engine, long runTime)
	{
		super();
		
		this._engine = engine;
		this._runTime = runTime;
	}

	// thread's run method
	public void run()
	{
		// run the threads for given time period
		try
		{
//System.out.println("Timer is sleeping for " + this._runTime/1000L + "seconds.");			
			Thread.sleep(this._runTime);	// milliseconds to sleep 
		} 
		catch (InterruptedException e) 
		{
			//eat the exception, not an error 
		}
		
		// timer has expired
		// notify Engine
//System.out.println("Timer has expired.");
		this._engine.timerEvent();
	}
	
	public void setTime(long time)
	{
		this._runTime = time;
	}
	public long getTime()
	{
		return(this._runTime);
	}
}
