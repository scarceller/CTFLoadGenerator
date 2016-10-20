/*
This class simplifies invoking a URL and handling the response data
Class also honors SESSIONID cookie by looking for and extracting the cookie from
the response and then allowing you to chain to the next request and automatically
propagating the cookie to the next chained request.
See the main() method for example on how to call and use this class:
Author: Sal Carceller IBM Corp.  
*/
package com.ibm.cpo.ctfload.workload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;

public class FriendlyURLRequest 
{
	private ClientWorkload _parent = null;
	private String _urlString = null; 
//	private URLConnection _connection = null;
	private List<String> _cookies = new ArrayList<String>(); // start with an empty List of cookies
	private byte[] _responseData = null;
	private int _responseCode = -1;
			
	public FriendlyURLRequest(String urlString, ClientWorkload parent)
	{
		super();
		this.initialize(urlString,parent);
	}
	
	public void initialize(String urlString, ClientWorkload parent)
	{
		this._parent = parent;
		this.setUrlString(urlString);
	}

	// used for unit testing this class
	public static void main(String[] args) 
	{
		FriendlyURLRequest req = null;
/*		
		String urlString1 = "http://10.81.124.202:9080/CTLBankWeb/welcome.jsp";
		String urlString2 = "http://10.81.124.202:9080/CTLBankWeb/App?action=login&uid=1&passwd=password";
		String urlString3 = "http://10.81.124.202:9080/CTLBankWeb/App?action=acctSummary";
		String urlString4 = "http://10.81.124.202:9080/CTLBankWeb/App?action=Deposit&amount=10&accID=101";
		String urlString5 = "http://10.81.124.202:9080/CTLBankWeb/App?action=Withdraw&amount=10&accID=101";
		String urlString6 = "http://10.81.124.202:9080/CTLBankWeb/App?action=custProfile";
		String urlString7 = "http://10.81.124.202:9080/CTLBankWeb/App?action=logout";
*/
/*		
		String urlString1 = "http://hotradenew.mybluemix.net/app";
		String urlString2 = "http://hotradenew.mybluemix.net/app?action=login&uid=uid:0&passwd=xxx";
		String urlString3 = "http://hotradenew.mybluemix.net/app?action=quotes&symbols=s:0,s:1,s:2,s:3,s:4";
		String urlString4 = "http://hotradenew.mybluemix.net/app?action=buy&symbol=s:1&quantity=10";
		String urlString5 = "http://hotradenew.mybluemix.net/app?action=portfolio";
		String urlString6 = "http://hotradenew.mybluemix.net/app?action=sell&holdingID=1";
		String urlString7 = "http://hotradenew.mybluemix.net/app?action=account";
		String urlString8 = "http://hotradenew.mybluemix.net/app?action=logout";
*/
		// build the list of URLs for the workload
		String urlString1 = "http://ctfbank.mybluemix.net";
		String urlString2 = "http://ctfbank.mybluemix.net/userLogon?userid=1&password=passw0rd";
		String urlString3 = "http://ctfbank.mybluemix.net/foo";
		String urlString4 = "http://ctfbank.mybluemix.net/processTransaction?amount=10.00&accountType=Checking";
		String urlString5 = "http://ctfbank.mybluemix.net/processTransaction?amount=-10.00&accountType=Checking";
		String urlString6 = "http://ctfbank.mybluemix.net/getUserProfile";
		String urlString7 = "http://ctfbank.mybluemix.net/userLogoff";
		
		for(int ii=1; ii<=10; ii++)
		{	
System.out.println("Executing WL #"+ii);			
			req = new FriendlyURLRequest(urlString1,null);	// create first request
			req = req.invoke( urlString2 );				// invoke first request and chain to next
			req = req.invoke( urlString3 );				// invoke next request and chain again
			req = req.invoke( urlString4 );				// ...
			req = req.invoke( urlString5 );
			req = req.invoke( urlString6 );
			req = req.invoke( urlString7 );
//			req = req.invoke( urlString8 );
			req.invoke();								// Finally, invoke last request with no chaining
		}
		
	}

	// use this invoke if not chaining requests
	public void invoke()
	{
		this.invoke(null);
	}
	// use this invoke if chaining commands
	public FriendlyURLRequest invoke( String nextUrlString )
	{
		URL url = null;
		FriendlyURLRequest nextRequest = null;
		URLConnection connection = null;
		List<String> cookies = null;
		
//System.out.println("- - - Calling request: " + this.getUrlString());
						
		try 
		{
				
			url = new URL( this.getUrlString() );
				
			connection = url.openConnection();
				        
			// copy cookies into the request's properties, if any
//System.out.println("Cookies = " + this._cookies );			
			if(this._cookies!=null)
			{
				// this._connection.addRequestProperty("Cookie", this._cookies);
				for( String cookie : this._cookies ) 
				{
			   		connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
//String tt = cookie.split(";", 2)[0];
//System.out.println(tt);
				}
			}
			
/*
//get all request parms
System.out.println("\n- - - Request Properties");			
			Map<String, java.util.List<String>> map = connection.getRequestProperties();
						for (Map.Entry<String, List<String>> entry : map.entrySet()) 
						{
							System.out.println
								(	"Key : " + entry.getKey() + 
					                " ,Value : " + entry.getValue()
					            );
						}
						
//get all headers
System.out.println("\n- - - Header Fields");			
			Map<String, java.util.List<String>> map2 = connection.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map2.entrySet()) 
			{
				System.out.println
					(	"Key : " + entry.getKey() + 
		                " ,Value : " + entry.getValue()
		            );
			}
*/			

			// get the HTTP response code and save it to this._responseCode
			if(connection instanceof java.net.HttpURLConnection)
			{	
				this._responseCode = ((java.net.HttpURLConnection)connection).getResponseCode();
			}
			
			// if we have a valid reply then fetch the reply data
			if( this._responseCode>=200 && this._responseCode<300 )
			{	
			  // process the response data, fully read the entire response
			  InputStream is = connection.getInputStream();

			  // super fast way to read the input stream!
			  ByteArrayOutputStream tempBuf = new ByteArrayOutputStream();
			  byte[] readBuf = new byte[4096];
			  int bytesRead = 0;
			  while((bytesRead = is.read(readBuf)) > -1)
			  {
				tempBuf.write(readBuf, 0, bytesRead);
			  }
			  this._responseData = tempBuf.toByteArray();
//System.out.println("- - -\n- - - Response Data = \n" + new String(this._responseData) );
//System.out.println("Response Code (" + this.getResponseCode() + ")");
			  
			  is.close();
			}
			else
			{ // response code was bad so we read no response data
//System.out.println("Invalid Reply, response code is " + this.getResponseCode());				
				this._responseData = null;
			}

			// If we are request chaining, build the next sequential request object
			if( nextUrlString != null )
			{
				// fetch any new cookies that may have been returned
				// if we got more new cookies then append them to the existing ones
				cookies = connection.getHeaderFields().get("Set-Cookie");
				if(cookies != null)
				{ // we just got more cookies back
					// Append any new cookies to the existing list of cookies
					this._cookies.addAll(cookies);
				}
				
				// build next request
				// copy cookies from current request to the next request
//System.out.println("Copy Cookies = " + this._cookies );			
				nextRequest = new FriendlyURLRequest(nextUrlString,this._parent);
				nextRequest._cookies = this._cookies;
			}
			
			connection = null;
		}
/*		
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
*/			
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return(nextRequest);
	}
	
	// after the request is invoked, this method returns the raw response
	public byte[] getResponseData()
	{
		return(this._responseData);
	}
	
	// Obtains the response code
	// You should only call this method after a request has been invoked
	// Will return -1 if the request has not yet been invoked
	// Under normal conditions valid response codes are:
	//   1xxx	these are partial replies, maybe just headers with no data
	//   2xxx	these are normal conditions and valid replies
	//   3xxx	these are redirect directives
	//	Error responses tend to be anything greater than 399 (>=400)
	//
	// For out performance tests we tend to only consider 2xxx as valid
	// anything else is counted as unexpected error
	public int getResponseCode()
	{
		return(this._responseCode);
	}
	
	// returns the url string for the request
	public String getUrlString()
	{
		return(this._urlString);
	}
	
	public void setUrlString(String urlString)
	{
		// if url has %uid% then we need to generate a unique userID
		int index = urlString.indexOf("%uid%"); 
		if( index > 0 )
		{ // url does contain %cid%, generate unique UserID and replace %cid%
			// chain back to parent ClientThread and generate the unique clientID
			long uniqueID = this._parent.getParent().generateUniqueUserID();
//System.out.println(this._parent.getClientID()+ ":" + uniqueID);			
			urlString = urlString.replaceAll( "%uid%", Long.toString(uniqueID) );
//System.out.println(this._parent.getClientID()+ ":" + urlString);
		}
		
		this._urlString = urlString;
	}
}
