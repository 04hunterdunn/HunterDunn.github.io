
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Add User
	</jsp:attribute>
    <jsp:attribute name="js">
        <script src="<c:url value='/resources/js/add_user.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="row">
            <div class="card narrow-centered">
                <div class="card-body">
                    <div class="page-title">Add User</div>
                    <form action="/dbqa/user/add" method="post" id="addUserForm" onsubmit="return validateAddUser()">
                        <div class="form-group">
                            <label for="email">Email</label>
                            <input type="text" class="form-control" name="email" id="email">
                        </div>
                        <div class="form-group">
                            <input type="checkbox" id="requireAuthentication" name="requireAuthentication" checked/>
                            Require authentication
                        </div>
                        <div id="passwordFields">
                            <div class="form-group">
                                <label for="password">Password</label>
                                <input type="password" class="form-control" id="password" name="password">
                            </div>
                            <div class="form-group">
                                <label for="verifyPassword">Verify Password</label>
                                <input type="password" class="form-control" id="verifyPassword" name="verifyPassword">
                            </div>
                        </div>
                        <button class="btn btn-primary" type="submit">Add User</button>
                        <button class="btn btn-secondary" type="button" onclick="window.location.href='<c:url value="/"/>'">Cancel</button>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>