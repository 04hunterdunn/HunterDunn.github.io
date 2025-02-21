<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Login
	</jsp:attribute>
	<jsp:attribute name="js">
        <script src="<c:url value='/resources/js/login.js'/>"></script>
    </jsp:attribute>
	<jsp:body>
		<div class="row">
			<div class="card narrow-centered">
				<div class="card-body">
					<div class="page-title">Login</div>
					<form method="post" action="/dbqa/login" onsubmit= "return validateLogin();">
						<div class="form-group">
							<label for="email">Email</label>
							<input class="form-control" type="text" id="email" name="email" required/>
						</div>
						<div class="form-group">
							<label for="email">Password</label>
							<input class="form-control" type="password" id="password" name="password" required/>
						</div>
						<button class="btn btn-primary" type="submit" name="Submit">Login</button>
						<button class="btn btn-secondary" type="button" name="Register" onclick="window.location.href='<c:url value="/user/add"/>'">Register</button>
					</form>
				</div>
			</div>
		</div>
	</jsp:body>
</t:layout>