<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Submit Query Type
	</jsp:attribute>
    <jsp:attribute name="bodyOnload">
		$('#querySelectionForm').submit()
	</jsp:attribute>
    <jsp:body>
        <div class="row">
            <div class="card narrow-centered">
                <div class="card-body">
                    <div class="page-title">Submit Query Type</div>
                    <form id="querySelectionForm" action="${launchUrl}" method="POST">
                        <c:forEach items="${signedParams}" var="signedParam">
                            <input type="hidden" name="${signedParam.getKey()}" value='${signedParam.getValue()}' />
                        </c:forEach>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>