<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Query
	</jsp:attribute>
	<jsp:attribute name="bodyOnload">
		highlightFirstFragment()
	</jsp:attribute>
	<jsp:attribute name="js">
		<script src="<c:url value='/resources/js/dbqa.js'/>"></script>
	</jsp:attribute>
	<jsp:body>
		<div class="row">
			<!-- Schema -->
			<div class="col-sm-3 top">
				<div class="card schema">
				<c:choose>
					<c:when test="${not user.requiresAuthentication}">
						<span style="text-align: center;">
							<h5>Database schema <a href="#" data-toggle="tooltip" title="Click on the table icon to see its data. Click on the table name to see its columns."><i class="fas fa-info-circle"></i></a></h5>
						</span>
					</c:when>
					<c:otherwise>
						<span>
							Database:
							<select id="currentDatabase" name="currentDatabase">
								<c:forEach items="${user.databases}" var="db">
									<option value="${db.databaseId}" <c:if test="${selectedDatabase.databaseId eq db.databaseId}">selected</c:if>>${db.name}</option>
								</c:forEach>
								<option value="${defaultDatabase.databaseId}" <c:if test="${selectedDatabase.databaseId eq defaultDatabase.databaseId}">selected</c:if>>${defaultDatabase.name}</option>
								<option value="addDatabase">Add new database...</option>
							</select>
						</span>
					</c:otherwise>
				</c:choose>
				<c:forEach items="${schema}" var="table" varStatus="loopStatus">
					<div>
						<i class="fas fa-table" style="cursor: pointer;" onclick="showTableData('tableData${loopStatus.index}')"></i>
						<a data-toggle="collapse" data-target="#${table.key}" onclick="toggle(this.firstElementChild)" style="cursor: pointer;">${table.key}
							<span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
						</a>
						<div id="${table.key}" class="collapse">
							<c:forEach items="${table.value}" var="column">
								&ensp;<span>&#8627;</span>${column}<br>
							</c:forEach>
						</div>
					</div>
					<br>
				</c:forEach>
				</div>
			</div>
			<div class="col-sm-9 top">
				<!-- Query Input -->
				<div class="card">
					<form method="post" name="queryForm" action="/dbqa/query">
						<input type="hidden" id="numParserResults" value="${parserResults.size()}"/>
						<input type="hidden" id="exceptionQuery" value="${exceptionQuery}"/>
						<input type="hidden" id="lti_consumer_key" value="${lti_consumer_key}"/>
						<input type="hidden" id="lis_outcome_service_url" value="${lis_outcome_service_url}"/>
						<input type="hidden" id="lis_result_sourcedid" value="${lis_result_sourcedid}"/>
						<input type="hidden" id="canvasUserId" value="${canvasUserId}"/>
						<input type="hidden" id="displayExplanation" value="${displayStepExplanation}"/>
						<div class="form-group">
							<label for="inputTextArea">Query:</label>
							<div id="parentDisplay" style="padding-bottom:10px;">
								<div class="form-control" id="displayDiv" style="height:140px; background-color:rgb(242,242,242);" <c:if test="${empty originalQuery}">onclick="addInputField();"</c:if>>
									<textarea class="form-control" id="inputTextArea" rows="6" name="query" required <c:if test="${empty exceptionQuery}">style="display:none;"</c:if>><c:choose><c:when test="${exceptionQuery != null}">${exceptionQuery}</c:when><c:otherwise>${originalQuery}</c:otherwise></c:choose></textarea>
								</div>
							</div>
							<button type="submit" id="submitBtn" class="btn btn-primary" onclick="toggleHtmlQuery()">Submit</button>
							<c:if test="${not empty originalQuery and sessionScope.user.requiresAuthentication}">
								<button type="button" id="editQueryBtn" class="btn btn-danger" onclick="addInputField()">Edit Query</button>
							</c:if>
							<button type="button" class="btn btn-secondary" style="display:none" id="prevBtn" onclick="prevClicked();"><i class="fas fa-arrow-alt-circle-left"></i> Previous</button>
							<button type="button" class="btn btn-secondary" style="display:none" id="nextBtn" onclick="nextClicked();">Next <i class="fas fa-arrow-alt-circle-right"></i></button>


							<label style="padding-left: 10px;"><input type="checkbox" id="highlightPrevious" name="highlightPrevious" value="true" <c:if test="${highlightPrevious}">checked</c:if>> Highlight Previous Steps</label>
							<label style="padding-left: 10px;"><input type="checkbox" id="displayStepExplanation" name="displayStepExplanation" value="true" <c:if test="${displayStepExplanation}">checked</c:if>> Display Step Explanation</label>

						</div>
					</form>
				</div>
				<div id="stepExplanation" class="card" <c:if test="${empty displayStepExplanation or not displayStepExplanation or empty originalQuery}">style="display: none"</c:if>><span id="stepExplanationText"></span></div>
				<c:forEach items="${tableData}" var="schemaTable" varStatus="loopStatus">
					<div id="tableData${loopStatus.index}" class="card" style="display:none">
						<h4>
							<i class="far fa-window-close" onclick="closeSchemaTable('tableData${loopStatus.index}')"></i>
							${schemaTable.getKey()}
						</h4>
						<div class="table-responsive">
							<table class="table table-sm table-striped table-hover">
								<thead>
									<tr>
										<c:forEach items="${schemaTable.getValue().getColumnNames()}" var="columnNames" varStatus="columnloop">
											<th>${columnNames}</th>
										</c:forEach>
									</tr>
								</thead>
								<tbody>
								<c:forEach items="${schemaTable.getValue().getData()}" var="data" varStatus="dataloop">
									<tr>
										<c:forEach items="${data}" var="row" varStatus="rowLoop">
											<td>${row}</td>
										</c:forEach>
									</tr>
								</c:forEach>
								</tbody>
							</table>
						</div>
					</div>
				</c:forEach>
				<c:forEach items="${parserResults}" var="parserResult" varStatus="thecount">
					<input type="hidden" id="queryFragment${thecount.count}" value="${parserResult.queryFragment}"/>
					<input type="hidden" id="fragmentIndex${thecount.count}" value="${parserResult.fragmentIndex}"/>
					<input type="hidden" id="nestingLevel${thecount.count}" value="${parserResult.queryNestingLevel}"/>
					<div class="card" id="parserResult${thecount.count}" <c:if test="${!thecount.first or not empty lti_consumer_key}">style="display:none"</c:if>>
						<c:if test="${parserResult.getQueryNestingLevel() > 0}">
							<div><label>Query: ${parserResult.getOriginalQuery()}</label></div>
						</c:if>
						<div class="table-responsive">
							<table class="table table-sm table-striped table-hover">
								<thead>
									<tr>
										<c:forEach items="${parserResult.getQueryResultSet().getColumnNames()}" var="columnNames" varStatus="columnloop">
											<th>${columnNames}</th>
										</c:forEach>
									</tr>
								</thead>
								<tbody>
								<c:forEach items="${parserResult.getQueryResultSet().getData()}" var="data" varStatus="dataloop">
									<tr>
										<c:forEach items="${data}" var="row" varStatus="rowLoop">
											<td>${row}</td>
										</c:forEach>
									</tr>
								</c:forEach>
								</tbody>
							</table>
						</div>
					</div>
				</c:forEach>
			</div>
		</div>
	</jsp:body>
</t:layout>