
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Add Database
	</jsp:attribute>
    <jsp:attribute name="js">
        <script src="<c:url value='/resources/js/add_database_type.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="row">
            <div class="card narrow-centered">
                <div class="card-body">
                    <div class="page-title">Add Database Type</div>
                    <form action="/dbqa/database/add_type" method="post" id="addDatabaseTypeForm" onsubmit="return validateAddDatabaseType()">
                        <div class="form-group">
                            <label for="name">Name</label>
                            <input type="text" class="form-control" name="name" id="name">
                        </div>
                        <div class="form-group">
                            <label for="driver">Driver</label>
                            <input type="text" class="form-control" name="driver" id="driver">
                        </div>
                        <div class="form-group">
                            <label for="dialect">Dialect</label>
                            <input type="text" class="form-control" name="dialect" id="dialect">
                        </div>
                        <div class="form-group">
                            <label for="catalog">Catalog</label>
                            <input type="text" class="form-control" name="catalog" id="catalog">
                        </div>
                        <div class="form-group">
                            <input type="checkbox" name="usernameAsSchema" id="usernameAsSchema">
                            Use Username for schema
                        </div>
                        <div class="form-group" id="schemaFormGroup">
                            <label for="schema">Schema</label>
                            <input type="text" class="form-control" name="schema" id="schema">
                        </div>
                        <button class="btn btn-primary" type="submit">Add Database Type</button>
                        <button class="btn btn-secondary" type="button" onclick="window.location.href='<c:url value="/database/add"/>'">Cancel</button>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>