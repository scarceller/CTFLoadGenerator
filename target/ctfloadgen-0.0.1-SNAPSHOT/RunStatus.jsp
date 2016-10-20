<%@page import=	"com.ibm.cpo.ctfload.engine.CTFLoadEngine" %>

<%
	// Obtain the engine and run status
	CTFLoadEngine engine = (CTFLoadEngine)request.getAttribute("loadEngine");
	int engineState = engine.getState();
	long runDuration = engine.getRunDuration()/1000L;
	
	long totalRunTime = engine.getTotalRuntime();
	long threadCount = engine.getThreadCount();
	long reqCount = Long.parseLong( engine.getEngineProperties().getProperty("numURLs") );
	long errorCount = engine.getTotalErrorCount();
	long totalWorkloadCount = engine.getTotalWorkloadCount();
	long totalReqCount = totalWorkloadCount * reqCount;
	float throughput = (float)engine.getTotalWorkloadCount() / (float)(totalRunTime / 1000);
	long responseTime = (int)((float)1000/(float)((float)throughput/(float)threadCount));
	
	// fetch 'polling' parameter from the request
	boolean polling = false;
	String parm = (String)request.getParameter("polling");
	if( (parm!=null) && parm.length()>0 && parm.charAt(0)=='t' )
	{ // polling parm is set to 'true'
		polling = true;
	}
%>

<html>
<head>
<title>Run Status</title>
<%
	// if we are not yet polling force polling
	if(polling!=true)
	{ // we have not yet entered polling mode, we force polling mode via a
		// http redirect using the META tag below.
		// we wait 3 seconds and then enter polling mode to check on the 
		// run's status
%>
<META http-equiv="refresh" content="3;URL=CmdServlet?action=status&polling=true"> 
<%	
	}
%>
</head>
<body>
	<h1><font color=blue>Run Status</font></h1>
<%
	// check the state of the run
	if(engineState == CTFLoadEngine.RUNNING)
	{ // engine is running
%>	
		<h2>Load engine is currently running for <%=runDuration%> seconds.</h2>
<%
		// should we poll the engine state till run finishes?
		if(polling==true)
		{ // we are polling for engine state
			float elapseTime = engine.getElapseTime();
			if(elapseTime>0)
				elapseTime = elapseTime / 1000L;	// convert to seconds
%>
	<hr>
	<h2><font color=green>... We are now polling the engine for run status ...</font></h2>
	
<!-- Report some stats on the run while it's running -->
<font size=+0 color=gray>
<ul>	
	<li>running with <%=engine.getThreadCount()%> threads.</li>
	<li>ran for <%=totalRunTime%> milliseconds.</li>
	<li>executed <%=totalWorkloadCount%> workloads.</li>
	<li><%=reqCount%> requests per workload.</li>
	<li>executed <%=totalReqCount%> requests.</li>
  <%
  if(errorCount==0)
  {	
  %>	
	<li><font color=lightgreen>All requests were successful.</font></li>
  <%
  }
  else
  {	
  %>
	<li><font color=red><%=errorCount%> requests failed.</font></li>
  <%
  }
  %>	

	<ins><br>Throughput per workload:</ins>
	<li><%=throughput%> workloads per second.</li>
	<li><%=responseTime%> milliseconds RT/Workload.</li>
</ul>
</font>
	
<font size=+2 color=black><b>	
<ul>
	<ins>Throughput per request:</ins>
	<li><%=throughput*reqCount%> requests per second.</li>
	<li><%=responseTime/reqCount%> milliseconds RT/Req.</li>
</ul>
</b></font>	
<!-- end of run stats -->
	
	<h3>Elapsed time: <font color=red><%=elapseTime%> seconds</font></h3>
	<hr>
	<h3><A href=CmdServlet?action=halt>Halt the run now</A></h3>
<%		
			// Set refresh, autoload time as 3 seconds
			// this forces the page to auto refresh every 3 seconds.
			response.setIntHeader("Refresh", 3);
		}
		else
		{ // we are not yet polling for engine state
%>
	<hr>		
	<H3><font color=green>... Please wait few seconds while we enter polling mode ...</font></H3>
	<h3><A href=CmdServlet?action=status&polling=true>Click here to start polling now</A></h3>
	<hr>
<%
		}
	}
	else
	{
%>	
		<p>Engine has finished running.
<%		
	}
%>	

</body>
</html>