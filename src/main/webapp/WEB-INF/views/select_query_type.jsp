<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Login
	</jsp:attribute>
	<jsp:attribute name="js">
        <script src="<c:url value='/resources/js/select_query_type.js'/>"></script>
    </jsp:attribute>
	<jsp:body>
		<div class="row">
			<div class="card narrow-centered">
				<div class="card-body">
					<div class="page-title">Select Query Type</div>
					<form method="post" name="itemSelectionForm" action="/dbqa/lti/1.1/select/send" onsubmit= "return validateSelection();">
						<input type="hidden" name="lti_consumer_key" value="${lti_consumer_key}"/>
						<input type="hidden" name="content_item_return_url" value="${lti_content_item_return_url}"/>
						<input type="hidden" name="lti_data" value="${lti_data}"/>

						<div class="form-group">
							<label for="queryType">Query Type</label>
							<div id="queryType">
								<input type="radio" name="queryType" value="select-from"/> SELECT-FROM<br>
								<input type="radio" name="queryType" value="select-from-where"/> SELECT-FROM-WHERE<br>
								<input type="radio" name="queryType" value="order-by"/> ORDER BY<br>
								<input type="radio" name="queryType" value="group-by-having"/> GROUP BY-HAVING<br>
								<input type="radio" name="queryType" value="join"/> JOIN<br>
								<input type="radio" name="queryType" value="pattern-matching"/> Pattern matching<br>
								<input type="radio" name="queryType" value="multiple-table-queries"/> Multiple tables<br>
								<input type="radio" name="queryType" value="aggregate-functions"/> Aggregate functions<br>
								<input type="radio" name="queryType" value="arithmetic-expressions"/> Arithmetic expressions<br>
								<input type="radio" name="queryType" value="subqueries"/> Subqueries
							</div>
						</div>
						<button class="btn btn-primary" type="submit" name="Submit">Select</button>
					</form>
				</div>
			</div>
		</div>
	</jsp:body>
</t:layout>