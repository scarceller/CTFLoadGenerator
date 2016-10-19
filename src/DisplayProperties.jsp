<%@page 
	import="java.util.Properties"
	import="java.util.List"
	import="java.util.ArrayList"
	import="java.util.Collections"
%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Display Engine Properties</title>
</head>

<body>
	<center><h1><font color=blue>Load Engine Properties</font></h1></center>

	<table border=1 >
		<thead><tr><th>Key</th><th>Value</th><th></th></tr></thead> 

<%
	Properties props = (Properties)request.getAttribute("EngineProperties");

	// first lets sort the keys		
	List<String> keys = new ArrayList<String>();
	for(String key : props.stringPropertyNames()) 
	{
  		keys.add(key);
	}
	Collections.sort(keys);		
	
	// now using the sorted keys lets get the values	
	for(int ii=0; ii<keys.size(); ii++ )
	{
		String key = keys.get(ii);
		String value = props.getProperty(key);
%>  
		
		<!--  put the key value pairs into rows in the table -->
		<tr>
  			<form action="CmdServlet?action=props&oper=update" method="post">
  				<td><input type="text" name="key" 	value="<%=key%>"	readonly></td>
  				<td><input type="text" name="value"	value="<%=value%>"	size="100"></td>
  				<td><input type="submit" value="Update"></td>
  			</form>
  		</tr>
<%  	
  	}	
%>
	</table>
<!-- 	
	<center><A href=CmdServlet?action=props&oper=reload&file=engine.properties><h3>Reload default engine.properties file</h3></A></center>
-->	
	<center><A href=CmdServlet?action=props&oper=list><h3>Reload engine properties from file</h3></A></center>
	<table border=1 >
		<thead><tr><th>New Key</th><th>Value</th><th></th></tr></thead>
		<tr>
  			<form action="CmdServlet?action=props&oper=add" method="post">
  				<td><input type="text" name="key" 	value="url??"></td>
  				<td><input type="text" name="value"	value="http://%host%:%port%/" size="100"></td>
  				<td><input type="submit" value="Add"></td>
  			</form>
  		</tr>
	</table>
	
	<A href=index.html><h1>Main Page</h1></A>
</body>

</html>