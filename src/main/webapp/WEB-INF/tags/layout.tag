<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="title" %>
<%@ attribute name="success" %>
<%@ attribute name="error" %>
<%@ attribute name="js" %>
<%@ attribute name="bodyOnload" %>

<!DOCTYPE html>
<html>
<head>
	<title>DBQA | ${title}</title>
	
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<!-- Latest compiled and minified CSS -->
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.13.1/css/all.min.css">
	<link rel="stylesheet" href="<c:url value='/resources/css/dbqa.css' />">
</head>
<body class="bg-dark" id="page-top" onload="${bodyOnload}">
	<c:if test="${empty sessionScope.user or sessionScope.user.requiresAuthentication}">
		<c:import url="/WEB-INF/views/template/navigation.jsp" />
	</c:if>
	<div class="content-wrapper">
		<div id="success" class="alert alert-success" role="alert" <c:if test="${empty success}">style="display:none"</c:if>>
			${success}
		</div>
		<div id="errors" class="alert alert-danger" role="alert" <c:if test="${empty error}">style="display:none"</c:if>>
			${error}
		</div>
		<div id="body">
			<jsp:doBody />
		</div>
	</div>
	<!-- jQuery library -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
	<!-- Popper JS -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
	<!-- FontAwesome 5 JS -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.13.1/js/all.min.js"></script>
	<!-- Latest compiled JavaScript -->
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
	${js}
</body>
</html>