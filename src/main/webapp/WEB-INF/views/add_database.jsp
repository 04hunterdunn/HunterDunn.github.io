
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Add Database
	</jsp:attribute>
    <jsp:attribute name="js">
        <script src="<c:url value='/resources/js/add_database.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="row">
            <div class="card narrow-centered">
                <div class="card-body">
                    <div class="page-title">Add Database</div>
                    <form action="/dbqa/database/add" method="post" id="addDatabaseForm" onsubmit="return validateAddDatabase()">
                        <div class="form-group">
                            <label for="databaseTypeId">Type</label>
                            <select id="databaseTypeId" name="databaseTypeId" class="form-control">
                                <c:forEach items="${databaseTypes}" var="dbType">
                                    <option value="${dbType.databaseTypeId}">${dbType.typeName}</option>
                                </c:forEach>
                            </select>
                            <a href="/dbqa/database/add_type">Add database type</a>
                        </div>
                        <div class="form-group">
                            <label for="name">Name</label>
                            <input type="text" class="form-control" name="name" id="name">
                        </div>
                        <div class="form-group">
                            <label for="url">URL</label>
                            <input type="text" class="form-control" name="url" id="url">
                        </div>
                        <div class="form-group">
                            <label for="username">Username</label>
                            <input type="text" class="form-control" name="username" id="username">
                        </div>
                        <div class="form-group">
                            <label for="password">Password</label>
                            <input type="password" class="form-control" name="password" id="password">
                        </div>
                        <button class="btn btn-primary" type="submit">Add Database</button>
                        <button class="btn btn-secondary" type="button" onclick="window.location.href='<c:url value="/query/${user.dbqaUserId}"/>'">Cancel</button>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>