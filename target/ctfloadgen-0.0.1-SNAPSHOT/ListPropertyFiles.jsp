<%@page 
	import="java.util.Iterator"
	import="java.util.List"
%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>List all load generation scripts</title>
</head>

<body>
	<center>
	<h1><font color=blue>List of all available load generation scripts</font></h1>
	
	<table border=1 cellpadding=2px >
		<thead><tr><th><h3>Please select the load generation script you wish to run</h3></th></tr></thead> 
<%
	List<String> propFiles = (List<String>)request.getAttribute("PropertyFilesList");

	Iterator<String> iterator = propFiles.iterator();
	String fileName = null;
	while(iterator.hasNext())
	{
		// obtain the name of the properties file
		fileName = iterator.next();
%>  
		<!-- render the properties fileName in the HTML -->
		<tr><td><A href=CmdServlet?action=props&oper=reload&file=<%=fileName%>><%=fileName%></A></td></tr>
<%  	
  	} // end of While() loop	
%>
	</table>
	</center>
	
	<hr>
	<ul>
	<li>Load generation parameters are specified in properties files which are located in the WAR file within the '...\WebContent\WEB-INF\classes' directory.
	<li>You may create new *.properties files within that directory as well as edit the existing files.
	<li>It's recommended you create/alter properties files within the WAR prior to deploying the WAR.
	<li>You may also directly create/alter these files after deployment within the application server, provided 
	the application sever allows you read/write access to the directory.
	</ul>
	<hr>		
	<h1><A href=index.html>Main Page</A></h1>
</body>

</html>