package com.ibm.cpo.ctfload.workload;

import com.ibm.cpo.ctfload.engine.ClientThread;
import com.ibm.cpo.utils.CPOLogger;

/*
TradeLite URLs:
Main Page	http://tradelitesal.ng.bluemix.net/tradelite/
Login Page	http://tradelitesal.ng.bluemix.net/tradelite/app
Login Cmd	http://tradelitesal.ng.bluemix.net/tradelite/app?action=login&uid=uid:0&passwd=xxx 
Logout Cmd	http://tradelitesal.ng.bluemix.net/tradelite/app?action=logout
*/

public class ClientWorkload 
{
	private ClientThread _parent = null;
	private Integer _clientID = null;
	private String[] _urlList = null; // hold the list of URLs to be invoked in sequential order
	private long _errorCount = 0;
	
	// test harness, used only for unit testing
	public static void main(String[] args) 
	{
		// build the list of URLs for the workload
		String[] urlList = new String[6];
		urlList[0] = "http://ctfbank.mybluemix.net/userLogon?userid=1&password=passw0rd";
		urlList[1] = "http://ctfbank.mybluemix.net/getAccountsForUser";
		urlList[2] = "http://ctfbank.mybluemix.net/processTransaction?amount=10.00&accountType=Checking";
		urlList[3] = "http://ctfbank.mybluemix.net/processTransaction?amount=-10.00&accountType=Checking";
		urlList[4] = "http://ctfbank.mybluemix.net/getUserProfile";
		urlList[5] = "http://ctfbank.mybluemix.net/userLogoff";

		
/* Tradelite URLs		
		// build the list of URLs for the workload
		String[] urlList = new String[8];
		urlList[0] = "http://tradelitesal.ng.bluemix.net/tradelite/app";
		urlList[1] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=login&uid=uid:0&passwd=xxx";
		urlList[2] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=quotes&symbols=s:0,s:1,s:2,s:3,s:4";
		urlList[3] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=buy&symbol=s%3A0&quantity=10";
		urlList[4] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=portfolio";
		urlList[5] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=sell&holdingID=";
		urlList[6] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=account";
		urlList[7] = "http://tradelitesal.ng.bluemix.net/tradelite/app?action=logout";
*/		
		
/* FBank URLs		
		String[] urlList = new String[7];
		urlList[0] = "http://10.81.124.202:9080/CTLBankWeb/welcome.jsp";
		urlList[1] = "http://10.81.124.202:9080/CTLBankWeb/App?action=login&uid=1&passwd=password";
		urlList[2] = "http://10.81.124.202:9080/CTLBankWeb/App?action=acctSummary";
		urlList[3] = "http://10.81.124.202:9080/CTLBankWeb/App?action=Deposit&amount=10&accID=101";
		urlList[4] = "http://10.81.124.202:9080/CTLBankWeb/App?action=Withdraw&amount=10&accID=101";
		urlList[5] = "http://10.81.124.202:9080/CTLBankWeb/App?action=custProfile";
		urlList[6] = "http://10.81.124.202:9080/CTLBankWeb/App?action=logout";
*/				
		ClientWorkload wl = new ClientWorkload(1,null);
		wl.setUrlList(urlList);
		wl.runWorkload();
	}
		
	
	public ClientWorkload(int clientID,ClientThread parent) 
	{
		super();
		
		CPOLogger.printDebug("ClientWorkload:ClientWorkload() constructor called.");
		
		this._parent = parent;
		this._clientID = clientID;
	}
	
	public ClientThread getParent()
	{
		return(this._parent);
	}
	public int getClientID()
	{
		return(this._clientID.intValue());
	}
	
	public void runWorkload()
	{
		String[] urlList = null;
		FriendlyURLRequest req = null;
		FriendlyURLRequest nextReq = null;
		int responseCode = -1;

		CPOLogger.printDebug("ClientWorkload[" + this._clientID + "]:runWorkload() called.");

		urlList = this._urlList;
		this._errorCount = 0;	// reset error counter
		
		// do we have a list of URLs to invoke?
		if( (urlList!=null) && (urlList.length>0) )
		{
			int listSize = this._urlList.length;
			for(int ii=0; ii<listSize; ii++)
			{ // invoke each URL in the list
				// test for 1st request
				if(ii==0)
					req = new FriendlyURLRequest(urlList[0],this);		// create the first request
				
				// test for last request in the list, the last request does not chain to another request
				if( ii != listSize-1 )
				{	
					nextReq = req.invoke( urlList[ii+1] );				// invoke each request and chain to next
					
					// Special processing for the sell command! 
					// if the next request is a action=sell then extract the last holdingID from 
					// this action= portfolio request
					if( this._urlList[ii+1].indexOf("action=sell") > 0 )
					{ // next request is a sell	
						String tempString = new String(req.getResponseData());
						int startIndex = tempString.lastIndexOf("holdingID=");
						if(startIndex>0)
						{
							startIndex = startIndex+10;
						
							// locate the next double quote "
							int endIndex = startIndex;
							while(true)
							{
								if(tempString.charAt(endIndex) == '>' )
								{
									endIndex = endIndex-1;
									break;
								}	
														
								endIndex++;
							}
						
							//tempString = tempString.trim();
							tempString = tempString.substring(startIndex, endIndex);
//System.out.println("> > > holdingID located (" + tempString + ")");
						
							// set the holdingId for the sell
							// just add the numeric holdingID to the end os the sell url
							nextReq.setUrlString( this._urlList[ii+1].concat(tempString) );
//System.out.println("> > > Next URL (" + nextReq.getUrlString() + ")");
						}
					} // end of special processing for sell
					
					responseCode = req.getResponseCode();
					req = nextReq;
				}	
				else // this is the last request in the list
				{	
					req.invoke();									// last request in the list does not chain
					responseCode = req.getResponseCode();
				}
				
				// test if request failed.
				// Good response code is 200-299, anything else is error
				if(responseCode<200 || responseCode>299)
				{	
					this._errorCount++;	// if error bump the errorCount
					// write error to error log.
					System.err.println("ERROR: Request (" + req.getUrlString() + ") has failed! Response Code " + responseCode);					
				}	
			}
		}
	}
	
	public void setUrlList(String[] list)
	{
		CPOLogger.printDebug("ClientWorkload:setProperties(Properties) called.");

		this._urlList = list;
	}

	public long getErrorCount()
	{
		return this._errorCount;
	}
}
