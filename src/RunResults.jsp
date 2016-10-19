<%@page import="com.ibm.cpo.ctfload.engine.CTFLoadEngine" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Run Results</title>
</head>
<body>
	<h1><font color=blue>Run Results</font></h1>	
<%
	// calculate all run statistics
	CTFLoadEngine engine = (CTFLoadEngine)request.getAttribute("loadEngine");
	long totalRunTime = engine.getTotalRuntime();
	long threadCount = engine.getThreadCount();
	long reqCount = Long.parseLong( engine.getEngineProperties().getProperty("numURLs") );
	long errorCount = engine.getTotalErrorCount();
	long totalWorkloadCount = engine.getTotalWorkloadCount();
	long totalReqCount = totalWorkloadCount * reqCount;
	float throughput = (float)engine.getTotalWorkloadCount() / (float)(totalRunTime / 1000);
	long responseTime = (int)((float)1000/(float)((float)throughput/(float)threadCount));
%>

<!-- Run Stats -->
<font size=+0 color=gray>
<ul>
	<ins>Overall statistics:</ins>
	<li>ran with <%=engine.getThreadCount()%> threads.</li>
	<li>ran in <%=totalRunTime%> milliseconds.</li>
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
<!-- End of run stats -->

</font>
	<h1><A href=index.html>Main Page</A></h1>
</body>
</html>