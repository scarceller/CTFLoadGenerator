package com.ibm.cpo.ctfload.engine;

import java.util.Properties;

import com.ibm.cpo.utils.CPOLogger;
import com.ibm.cpo.utils.CPORandomNumber;
import com.ibm.cpo.ctfload.workload.ClientWorkload;

public class ClientThread extends Thread
{
	private Integer _clientThreadID = null;
	private Properties _clientProperties = null;
	private String[] _urlList = null;
	private boolean _haltIndicator = false;
	private long _workLoadCounter = 0;
	private long _errorCount = 0;
	private long _uidStartID = 0;	// lowest userID to be used by the engine
	private long _uidBlockSize = 0;	// block size for generating userIDs
	private CPORandomNumber _rad = new CPORandomNumber();	// CPO random number generator
	
	public ClientThread(Integer clientThreadID)
	{
		super(clientThreadID.toString());
		
		CPOLogger.printDebug("ClientThread:ClientThread(" + clientThreadID + ") constructor called.");	
		
		this._clientThreadID = clientThreadID;
//System.out.println("! ! ! ThreadID=" + this._clientThreadID);		
	}
	
	public void run()
	{
		CPOLogger.printDebug("ClientThread:run(" + this._clientThreadID + ") called.");
		this._workLoadCounter = 0;	// set the workload counter to Zero
		this._errorCount = 0;		// set the error counter to 0

		// build the list of URLs for the workload
		//int tid = this._clientThreadID;

/*		
		// List of TradeLite URLs
		String[] urlList = new String[8];
		urlList[0] = "http://tradelitesal.ng.bluemix.net/tradelite/app";
		urlList[1] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=login&uid=uid:" + tid + "&passwd=xxx";
  		urlList[2] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=quotes&symbols=s:0,s:1,s:2,s:3,s:4";
		urlList[3] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=buy&symbol=s%3A" + tid*10 + "&quantity=10";
		urlList[4] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=portfolio";
		urlList[5] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=sell&holdingID=";
		urlList[6] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=account";
		urlList[7] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=logout"; 
*/

/*		
        // List of FBank URLs		
		String[] urlList = new String[7];
		urlList[0] = "http://10.84.253.226:9080/CTLBankWeb/welcome.jsp";
		urlList[1] = "http://10.84.253.226:9080/CTLBankWeb/App?action=login&uid=" + tid + "&passwd=password";
		urlList[2] = "http://10.84.253.226:9080/CTLBankWeb/App?action=acctSummary";
		urlList[3] = "http://10.84.253.226:9080/CTLBankWeb/App?action=Deposit&amount=10&accID=" + tid + "01";
		urlList[4] = "http://10.84.253.226:9080/CTLBankWeb/App?action=Withdraw&amount=10&accID=" + tid + "01";
		urlList[5] = "http://10.84.253.226:9080/CTLBankWeb/App?action=custProfile";
		urlList[6] = "http://10.84.253.226:9080/CTLBankWeb/App?action=logout";
*/
		
		ClientWorkload workload = new ClientWorkload(this._clientThreadID,this);
		workload.setUrlList(this._urlList);
				
		// just keep calling the workload till told to halt
		this._haltIndicator = false;
		while( this._haltIndicator == false )
		{
			workload.runWorkload();
			_errorCount = _errorCount + workload.getErrorCount();	// check for errors
			this._workLoadCounter++;	// bump the work load counter
		}	
			
		return;
	}
	
	public void halt()
	{
		CPOLogger.printDebug("ClientThread:halt(" + this._clientThreadID + ") called.");
		
		this._haltIndicator = true;
	}	
	
	public long getWorkloadCounter()
	{
		return(this._workLoadCounter);
	}
	
	public long getErrorCount()
	{
		return this._errorCount;
	}
		
	public void setProperties(Properties props)
	{
		CPOLogger.printDebug("ClientThread:setProperties(Properties) called.");

		// make a deep copy (clone) of the properties, each thread needs it own dedicated properties. 
		this._clientProperties = new Properties(props);
		this.processProperties();	// process the properties for this thread
	}
	
	public long generateUniqueUserID()
	{
		long userID = 0;
		long blockSize = 0;
		int blockNumber = 0; 
				
		blockSize = this._uidBlockSize;					// number of userIDs possible for this thread
		blockNumber = this._clientThreadID.intValue();	// clientID block number for this thread
														// same as threadID thread1=1, thread2=2, ...
		
		userID = this._rad.nextLong();					// generate a random long
		userID = (userID % blockSize)+1;				// make the random in the range of 1-blockSize
														// by using divide for remainder
		
		userID = (blockNumber-1)*blockSize+userID;		// fit the userID to the block for this thread
		userID = this._uidStartID-1+userID;				// adjust the userID to the startID
				
		return(userID);
	}
	
//------------------------------------------------------------------------------	
// private methods start here	
//------------------------------------------------------------------------------
	// process properties does things like:
	// - calculate unique user ID range forthe given thread. 
	//   Each thread has a block of userids it may use during login.
	// - Populate the this._urlList array of URLs to be called.
	//   We want an array of urls for extremely fast performance. 
	private void processProperties()
	{
		// build the array list of URLs to be called
		this.buildUrlListArray();
		
		// setup the unique userID generator for this client thread 
		this.setupUserIdGenerator();
	}
	
	private void buildUrlListArray()
	{
		String propKey = null;
		String propValue = null;
		int numURLs = 0;
		
		// obtain the total number of urls to be called. 
		propValue = this._clientProperties.getProperty("numURLs");
		numURLs = Integer.parseInt(propValue);
		
		// intatiate the array list of urls
		this._urlList = new String[numURLs];
		
		propKey = "url";
		for( int ii=1; ii<=numURLs; ii++ )
		{
			propValue = this._clientProperties.getProperty(propKey + ii);
			
//System.out.println("key(" + propKey+ii + ") : value(" + propValue + ")");
		
			// add each url to the array of urls
			this._urlList[ii-1] = propValue;
		}
	}
	
	private void setupUserIdGenerator()
	{
		String propValue = null;
		long uidStart = 0;
		long uidEnd = 0;
		int numClients = 0;
		long uidTotalRange = 0;

		propValue = this._clientProperties.getProperty("uidStart");
		uidStart = Long.valueOf(propValue);
		this._uidStartID = uidStart; 
		
		propValue = this._clientProperties.getProperty("uidEnd");
		uidEnd = Long.valueOf(propValue);

		propValue = this._clientProperties.getProperty("numClients");
		numClients = Integer.valueOf(propValue);
		
		uidTotalRange = uidEnd-uidStart+1;
		
		this._uidBlockSize = uidTotalRange/numClients;
	}

}
