// Singleton class, only 1 instance allowed!
// use com.ibm.cpo.fload.engine.FLoadEngine.getInstance() to obtain engine instance. 

package com.ibm.cpo.ctfload.engine;

import com.ibm.cpo.utils.CPOLogger;
import java.util.Properties;

public class CTFLoadEngine 
{
	private static final CTFLoadEngine INSTANCE = new CTFLoadEngine();
	
	private int _engineState = 0;									// State of engine:
	public static final int READY = 0;								//   0=ready			
	public static final int RUNNING = 1;							//   1=running
	public static final int DONE = 2;								//   2=DONE
	
	//private boolean _firstTimeInd = true;
	private Properties _engineProperties = null;
	private int _numClients = 0;
	private ClientThread[] _threadGroup = null;
	
	private EngineRunTimer _timer = null;
	
	private long _startTime = 0;
	private long _endTime = 0;
	private long _totalRunTime = 0;
	
/*	
	public static void main(String[] args) 
	{
		CPOLogger.setDebugIndicator(true);
		CPOLogger.printDebug("FLoadEngine:main() called.");		
		
		// obtain the engine instance
		FLoadEngine engine = FLoadEngine.getInstance();
	}
*/	
	
	/* Here we are not creating Singleton instance inside getInstance() method 
	 * instead it will be created by ClassLoader. 
	 * Also private constructor makes impossible to create another instance , except one case. 
	 * You can still access private constructor by reflection and calling setAccessible(true). 
	 * By the way You can still prevent creating another instance of Singleton by this way by 
	 * throwing Exception from constructor.
	 * Read more: 
	 * http://javarevisited.blogspot.com/2012/12/how-to-create-thread-safe-singleton-in-java-example.html#ixzz34KzagVKf
	 */
	public static CTFLoadEngine getInstance()
	{
/*		
		// if first time engine has been referenced
		// load the default engine properties from the properties file 'engine.properties'
		if( INSTANCE._firstTimeInd == true )
		{
			INSTANCE._firstTimeInd = false;
			INSTANCE.setEngineProperties(engineProperties);
		}
*/		
        return INSTANCE;
    }

	// initializes the engine back to ready state
	public void initializeEngine()
	{
		this.halt();	// just in case engine is running halt it first 
		
		// if timer is running interrupt it and kill it
		if(this._timer != null)
		{	
			this._timer.interrupt();
//			this._timer = null;
		}
		
		this._engineState = CTFLoadEngine.READY;
		this._engineProperties = null;
		this._threadGroup = null;
		this._numClients = 0;
		this._startTime = 0;
		this._endTime = 0;
		this._totalRunTime = 0;
	}

	// set engine properties
	public void setEngineProperties(Properties engineProperties)
	{
		CPOLogger.printDebug("FLoadEngine:setEngineProperties(Properties) called.");
		
		// make a deep copy of the engine properties
		this._engineProperties = new Properties(engineProperties);	// set the properties for the engine
		this.processEngineProperties();								// process the properties
	}
	// get engine properties
	public Properties getEngineProperties()
	{
		CPOLogger.printDebug("FLoadEngine:getEngineProperties() called.");
		
		return(this._engineProperties);
	}
	
	public void createClientThreads(Integer numClients)
	{
		this._numClients = numClients.intValue();
		this._threadGroup = new ClientThread[this._numClients];
		
		// create n number of threads
		for(int ii=0; ii<this._numClients; ii++)
		{	
			ClientThread t1 = createClientThread(ii+1);	// create a thread
			this._threadGroup[ii] = t1;				// add the created thread to the thread group list
		}
	}
	
	/*
	 * Run the engine, start all client threads running
	 */
	public void run(long time)
	{
		int numThreads = 0;
		
		// do we have threads ready?
		numThreads = this._threadGroup.length;
		if( numThreads > 0)
		{
			// record the current time for start of run
			this._startTime = System.currentTimeMillis();
			
			// start all threads running!
			for(int ii=0; ii<numThreads; ii++)
				this._threadGroup[ii].start();
	
			// create the timer for the run and start the timer
			// when timer expires it will invoke the timerEvent() method in this class
			this._timer = new EngineRunTimer(this, time);
			this._timer.start();	// start the timer
		
			// mark engine in RUNNING state
			this._engineState = CTFLoadEngine.RUNNING;
		}

	}

	/*
	 * Halt the engine, stop all client threads neatly
	 */
	public void halt()
	{
		int numThreads = 0;
		
		CPOLogger.printDebug("FLoadEngine:halt() called.");
		
		// is engine running?
		if(this._engineState == CTFLoadEngine.RUNNING)
		{ // engine is running, halt it	
			// do we have threads ready?
			numThreads = this._threadGroup.length;
			if( numThreads > 0)
			{
				// halt all threads
				for(int ii=0; ii<numThreads; ii++)
				{	
					this._threadGroup[ii].halt();	// tell thread to halt
				}	
			}
		
			// set engine's state to DONE
			this._engineState = CTFLoadEngine.DONE;
		
			// record the current time for end of run and calculate the runs elapse time
			this._endTime = System.currentTimeMillis();
			this._totalRunTime = this._endTime - this._startTime;
		}
	}
	
	// returns the state of the engine
	// Valid states:
	// 		READY = 0;											
	// 		RUNNING = 1;			
	// 		DONE = 2;				
	public int getState()
	{
		return(this._engineState);
	}
	
	// Returns how many times the Engine invoked the workload
	// only to be called after a run completes or while run is running
	// if run is not in DONE state we return -1
	public long getTotalWorkloadCount()
	{
		long totalCount = 0;
		
		// be sure engine is done running
		if( (this._engineState == CTFLoadEngine.DONE) || (this._engineState == CTFLoadEngine.RUNNING) )
		{	
			// tally up how many times each thread executed the workload
			ClientThread[] threads = this._threadGroup;
			int numThreads = threads.length;
			for(int ii=0; ii<numThreads; ii++)
			{
				totalCount = totalCount + threads[ii].getWorkloadCounter(); 
			}
		}
		else
		{ // ERROR: Engine is not in DONE or RUNNING state
			totalCount = -1;
		}
		
		return(totalCount);
	}
	
	// Returns how many requests failed during the run
	// only to be called after a run completes or while the run is in progress.
	// if run is not in DONE or RUNNING state we return -1
	public long getTotalErrorCount()
	{
		long totalErrorCount = 0;
		
		// be sure engine is done or is running
		if( (this._engineState == CTFLoadEngine.DONE) || (this._engineState == CTFLoadEngine.RUNNING) )
		{	
			// tally up how many errors where encountered
			ClientThread[] threads = this._threadGroup;
			int numThreads = threads.length;
			for(int ii=0; ii<numThreads; ii++)
			{
				totalErrorCount = totalErrorCount + threads[ii].getErrorCount(); 
			}
		}	
		else
		{ // ERROR: Engine is not in DONE state
			totalErrorCount = -1;
		}
		
		return(totalErrorCount);
	}
	
	// returns the number of threads in the Workload Engine
	public int getThreadCount()
	{
		int threadCount = 0;
		
		threadCount = this._threadGroup.length;
		
		return(threadCount);
	}
	
	// After a run has finished (state=DONE) returns the total elapse time for the run
	// or if in state=RUNNING returns the length of time it's run so far
	// returns -1 if engine is Not DONE
	public long getTotalRuntime()
	{
		long totalRunTime = -1; // assume engine is not in the DONE state!
		
		switch(this._engineState)
		{
			case CTFLoadEngine.DONE:
				totalRunTime = this._totalRunTime;
			break;
			
			case CTFLoadEngine.RUNNING:
				// calculate the current runs elapse time
				totalRunTime = System.currentTimeMillis() - this._startTime;
			break;	
		}
		
		return(totalRunTime);
	}
	
	// if engine is not running returns -1
	// if running it returns the amount of time (elapsed time) in milliseconds 
	// that the engine has been running for.
	public long getElapseTime()
	{
		long elapseTime = -1;	// assume engine is not running
		
		if(this._engineState == CTFLoadEngine.RUNNING)
		{
			elapseTime = System.currentTimeMillis() - this._startTime;
		}
		
		return(elapseTime);
	}
	
	public long getRunDuration()
	{
		long runDuration = -1;
		
		// if we have a timer, get the timer's time
		if(this._timer!=null)
			runDuration = this._timer.getTime();
				
		return(runDuration);
	}
	
//------------------------------------------------------------------------------	
// package scope methods start here	
//------------------------------------------------------------------------------
	// Timer thread calls this method when timer expires
	// this halts the run
	void timerEvent()
	{
		this.halt();			// halt the run, stop all threads!
		this._timer = null;		// timer is done, destroy it
	}
	
//------------------------------------------------------------------------------	
// private methods start here	
//------------------------------------------------------------------------------
	private void processEngineProperties()
	{
		String propKey = null;
		String propValue = null;
		String host = null;
		String port = null;
		String urlAnchor = null;
		int numURLs = 0;
		
		// obtain target host:port properties
		host = this._engineProperties.getProperty("host");
		port = this._engineProperties.getProperty("port");
		// obtain the urlAnchor and replace host:port
		urlAnchor = this._engineProperties.getProperty("mainUrlAnchor");
		urlAnchor = urlAnchor.replaceAll("%host%", host);
		urlAnchor = urlAnchor.replaceAll("%port%", port);
		
		// obtain each url1, url2, url3, ... from the props and replace [host]:[port]
		int ii = 1;
		propKey = "url";
		while( (propValue = this._engineProperties.getProperty(propKey + ii)) != null )
		{
			propValue = propValue.replaceAll("%mainUrlAnchor%", urlAnchor);
//			propValue = propValue.replaceAll("%urlhost%", host);
//			propValue = propValue.replaceAll("%port%", port);
			
			this._engineProperties.setProperty(propKey + ii, propValue);
//System.out.println("key(" + propKey+ii + ") : value(" + this._engineProperties.getProperty(propKey + ii) + ")");

			ii++;
		}
		numURLs = ii-1;
		
		this._engineProperties.setProperty( "numURLs", String.valueOf(numURLs) );
//System.out.println("key(numURLs) : value(" + this._engineProperties.getProperty("numURLs") + ")");
	}
	
	private ClientThread createClientThread(Integer clientThreadID)
	{
		ClientThread clientThread = new ClientThread(clientThreadID);
		clientThread.setProperties(this.getEngineProperties());
		
		return(clientThread);
	}
	
	// hide the constructor, this is a singleton object
	private CTFLoadEngine() 
	{
		CPOLogger.printDebug("FLoadEngine:FLoadEngine() constructor called.");		
				
		if( INSTANCE != null )
		{ // Do NOT allow constructor to be called more than once!	
			java.lang.RuntimeException e = new java.lang.RuntimeException
				("FLoadEngine() consructor should never be called! This is a singleton instance!");
			
			throw(e);
		}	
	}

}
