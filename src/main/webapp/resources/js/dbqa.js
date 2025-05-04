let query = "";
let htmlQuery = "";
let currentParserResultNum = 1; //to correspond with JSTL loop counter
let fragmentList = [];
let fragmentIndexList = [];
let maxScore = 0;

$(document).ready(function() {
	$("#currentDatabase").change(function() {
		const selectedDatabase = $(this).val();
		if (selectedDatabase === 'addDatabase') {
			window.location.href = '/dbqa/database/add'
		} else if(selectedDatabase !== '') {
			window.location.href = '/dbqa/database/'+ selectedDatabase;
		}
	});

	//links with the checkbox in query.jsp for the highlight
	$("#highlightPrevious").click(function () {
		highlightPrevious();
		if ($("#highlightPrevious").prop("checked") == true) {
			logInteraction("highlight previous fragments");
		} else {
			logInteraction("unhighlight previous fragments");
		}
	});

	//add step explanation here? or call add highlight and generate step explanation??
	$("#displayStepExplanation").click(function () {
		if($("#displayStepExplanation").prop("checked") == true) {
			document.getElementById("stepExplanation").style.display = "block";
		}
		else{
			document.getElementById("stepExplanation").style.display = "none";
		}
	});

	if($("#exceptionQuery").val() !== "") {
		query = $("#exceptionQuery").val();
		document.getElementById("parentDisplay").innerHTML='<textarea class="form-control" id="inputTextArea" rows="6" name="query" required>'+query+'</textarea>';
	}

	$('[data-toggle="tooltip"]').tooltip();
});

function highlightPrevious() {
	if ($("#highlightPrevious").prop("checked") == true) {
		highlightPrev();
	} else {
		removeHighlightPrev();
	}
}

//called by query.jsp body onload event
function highlightFirstFragment() {
	query = document.getElementById("inputTextArea").value;
	if(query !== "" && $("#exceptionQuery").val() === "") {
		//request comes from a tool consumer
		if($("#lti_consumer_key").val()) {
			currentParserResultNum=0;
			htmlQuery = query.replace(/\n/g, '<br>');
			document.getElementById("displayDiv").innerHTML= htmlQuery;
			$("#nextBtn").text("Submit");
			$("#nextBtn").removeClass("btn-secondary");
			$("#nextBtn").addClass("btn-primary");
		}
		//request does not come from a tool consumer
		else {
			$("displayDiv").empty();
			let frag = $("#queryFragment" + currentParserResultNum).val();
			let fragInd = updateFragInd(parseInt($("#fragmentIndex" + currentParserResultNum).val()));
			fragmentList.push(frag);
			fragmentIndexList.push(fragInd);
			handleCurrentFragment(frag, fragInd);
		}
		$("#submitBtn").hide();
		$("#nextBtn").show();
		$("#prevBtn").prop("disabled", true);
		$("#prevBtn").show();
	}
}

function prevClicked() {
	if (fragmentList != null && fragmentIndexList != null) {
		fragmentList.pop();
		fragmentIndexList.pop();
	}
	$("#parserResult" + currentParserResultNum).hide();

	let prevParserResultNum = currentParserResultNum-1;
	//if we're now displaying the final parserResult for a subquery, show prior parserResult at lower nesting level
	if($("#nestingLevel"+prevParserResultNum).val() > $("#nestingLevel"+currentParserResultNum).val()) {
		for(let i=prevParserResultNum-1; i>=0; i--) {
			if($("#nestingLevel"+i).val() < $("#nestingLevel"+prevParserResultNum).val()) {
				$("#parserResult" + i).show();
				break;
			}
		}
	}

	currentParserResultNum = prevParserResultNum;
	$("#parserResult"+currentParserResultNum).show();

	let frag = $("#queryFragment"+currentParserResultNum).val();
	let fragInd = updateFragInd(parseInt($("#fragmentIndex"+currentParserResultNum).val()));
	handleCurrentFragment(frag, fragInd);
	highlightPrevious();
	$("#nextBtn").prop("disabled", false);
	if(currentParserResultNum === 1) {
		$("#prevBtn").prop("disabled", true);
	}
	logInteraction('previous');
}

function nextClicked() {
	let nextParserResultNum = currentParserResultNum+1;

	if(currentParserResultNum > 0) {
		//if next parserResult is at same or lower nesting level, hide current parserResult
		if ($("#nestingLevel" + nextParserResultNum).val() <= $("#nestingLevel" + currentParserResultNum).val()) {
			$("#parserResult" + currentParserResultNum).hide();
		}
		//if we've just finished displaying parserResults for a subquery, hide parserResult at same nesting level
		if ($("#nestingLevel" + nextParserResultNum).val() < $("#nestingLevel" + currentParserResultNum).val()) {
			for (let i = currentParserResultNum - 1; i >= 0; i--) {
				if ($("#nestingLevel" + i).val() === $("#nestingLevel" + nextParserResultNum).val()) {
					$("#parserResult" + i).hide();
					break;
				}
			}
		}
		$("#prevBtn").prop("disabled", false);
	} else {
		$("#nextBtn").text("Next");
		$("#nextBtn").removeClass("btn-primary");
		$("#nextBtn").addClass("btn-secondary");
	}
	currentParserResultNum = nextParserResultNum;

	let frag = $("#queryFragment"+currentParserResultNum).val();
	let fragInd = updateFragInd(parseInt($("#fragmentIndex"+currentParserResultNum).val()));
	fragmentList.push(frag);
	fragmentIndexList.push(fragInd);
	$("#parserResult"+currentParserResultNum).show();
	handleCurrentFragment(frag, fragInd);
	highlightPrevious();
	if(currentParserResultNum >= parseInt($("#numParserResults").val())) {
		$("#nextBtn").prop("disabled", true);
	}
	if($("#lis_outcome_service_url").val()) {
		notifyToolConsumer();
	}
	logInteraction('next');
}

function notifyToolConsumer() {
	const score = currentParserResultNum/$("#numParserResults").val();
	if(score > maxScore) {
		const url = "/dbqa/lti/1.1/result/send";
		jQuery.ajax(
			url, {
				async: false,
				method: "POST",
				data: {consumerKey: $("#lti_consumer_key").val(), serviceUrl: $("#lis_outcome_service_url").val(), sourcedid: $("#lis_result_sourcedid").val(), score: score},
				error: () => {
					console.log("error when sending score through lti");
				}
			});

		maxScore = score;
	}
}

function logInteraction(interactionType) {
	const url = "/dbqa/query/log";
	jQuery.ajax(
		url, {
			async: true,
			method: "POST",
			data: {canvasUserId: $("#canvasUserId").val(), sourcedid: $("#lis_result_sourcedid").val(), query: query, currentStep: currentParserResultNum, totalSteps: $("#numParserResults").val(), interactionType: interactionType, stepExplanation: $("#displayExplanation").val()},
			error: () => {
				console.log("error when logging interaction");
			}
		});
}

function addInputField() {
	$("#displayDiv").remove();
	document.getElementById("parentDisplay").innerHTML='<textarea class="form-control" id="inputTextArea" rows="6" name="query" required>'+query+'</textarea>';

	//set cursor
	document.getElementById("inputTextArea").focus(); //sets focus to element
	var val = document.getElementById("inputTextArea").value; //store the value of the element
	document.getElementById("inputTextArea").value = ''; //clear the value of the element
	document.getElementById("inputTextArea").value = val; //set that value back.
	$("#editQueryBtn").hide();
	$("#nextBtn").hide();
	$("#prevBtn").hide();
	$("#submitBtn").show();
}

function highlightPrev() {
	for(var i=0; fragmentList != null && i<fragmentList.length-1; i++) {
		addHighlightingAndExplanation(fragmentList[i], fragmentIndexList[i], '#0ACBEE');
	}
}

function removeHighlightPrev() {
	var openSpanInd = -1;
	var closeSpanInd = -1;
	//while no more blue span tags are found
	do {
		openSpanInd = htmlQuery.indexOf('<span style=\'background-color:#0ACBEE\'>', openSpanInd+1);
		if(openSpanInd != -1) {
			closeSpanInd = htmlQuery.indexOf('</span>', openSpanInd+1)
			htmlQuery = htmlQuery.substr(0,closeSpanInd) + htmlQuery.substr(closeSpanInd+7, htmlQuery.length);
			htmlQuery = htmlQuery.replace('<span style=\'background-color:#0ACBEE\'>', '');
		}
	} while(openSpanInd != -1);
	document.getElementById("displayDiv").innerHTML= htmlQuery;
}

function newLimit() {
	var newL = document.getElementById("newLimit").value;
	var dbqaTable = document.getElementById("tbody");
	for (var i = 0; i<dbqaTable.rows.length; i++) {
		row = dbqaTable.rows[i];
		if (i<newL) {
			row.style.display = '';
		}
		else {
			row.style.display = 'none';	
		}
	}
	return true;		
}	

function toggle(element) {
	console.log("toggle()");
	console.log(element);
	
	if (element.classList.contains("glyphicon-chevron-right")) {
		console.log("chevron-right");
		$(element).removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
	} else {
		console.log("chevron-left");
		$(element).removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right");
	}
}

function handleCurrentFragment(fragment, fragIndex) {
	//start with unhighlighted html query
	htmlQuery = query.replace(/\n/g, '<br>');
	addHighlightingAndExplanation(fragment, fragIndex, 'yellow');
}

function addHighlightingAndExplanation(fragment, fragIndex, color) {
	var stepFragment = fragment;
	//processing WHERE or HAVING clause
	//changed this to account for Intersect and Except
	if(!fragment.toUpperCase().startsWith("FROM") && !fragment.toUpperCase().startsWith("SELECT") && !fragment.toUpperCase().startsWith("LEFT") && !fragment.toUpperCase().startsWith("JOIN") && !fragment.toUpperCase().startsWith("GROUP BY") && !fragment.toUpperCase().startsWith("ORDER BY") && !fragment.toUpperCase().startsWith("UNION") && !fragment.toUpperCase().startsWith("INTERSECT") && !fragment.toUpperCase().startsWith("EXCEPT")) {
		highlightWhereOrHaving(fragment, fragIndex, color);
	}
	//processing SELECT clause with a grouping function and grouped columns
	else if(fragment.toUpperCase().startsWith("SELECT") && fragment != htmlQuery.substr(updateHighlightedFragInd(fragIndex), fragment.length)) {
		if(color == 'yellow') {
			highlightSelectWithoutGroupingFunctions(fragment, fragIndex, color);
		}
		//highlighting previous should make some use of blue
		else {
			highlightSelectWithoutGroupingFunctions(fragment, fragIndex, color);

			//and yellow if there aren't additional fragments
			if(fragmentList.indexOf(fragment) == fragmentList.length-1) {
				highlightGroupingFunctionsInSelect('yellow');
			}
		}
	}
	//processing GROUP BY clause
	else if(fragment.toUpperCase().startsWith("GROUP BY")) {
		highlightGroupByWithGroupingFunctions(fragment, fragIndex, color);
	}
	else {
		fragIndex = updateHighlightedFragInd(fragIndex);
		var updatedFragment = fragment.replace(/\n/g, '<br>');
		var endIndex = updatedFragment.length + fragIndex;
		if (!htmlQuery.substr(fragIndex, fragIndex.length).startsWith(updatedFragment)) {
			endIndex = updateHighlightedFragIndFromIndex(fragIndex, fragIndex+updatedFragment.length);
		}
		htmlQuery = htmlQuery.substr(0,fragIndex)+"<span style='background-color:"+color+"'>"+updatedFragment+"</span>"+htmlQuery.substr(endIndex);
		stepFragment = updatedFragment;
	}
	document.getElementById("displayDiv").innerHTML= htmlQuery;
	if(color === 'yellow') {
		generateStepExplanation(stepFragment);
	}
}

function generateStepExplanation(fragment) {
	fragment = fragment.trim();
	let upperCaseFragment = fragment.toUpperCase();
	if(upperCaseFragment.startsWith("FROM ")) {
		if(upperCaseFragment.indexOf(',') === -1) {
			$("#stepExplanationText").text("Retrieve all rows from: " + fragment.substr(5));
		} else {
			$("#stepExplanationText").text("Retrieve all rows from a cartesian product of: " + fragment.substr(5));
		}
	}
	else if(upperCaseFragment.startsWith("SELECT ")) {
		let starIndex = upperCaseFragment.indexOf('*');
		//if select statement is a select all
		if(starIndex != -1 && upperCaseFragment.charAt(starIndex-1) != '.') {
			$("#stepExplanationText").text("Retain all columns");
		} else {
			$("#stepExplanationText").text("Remove all columns other than: " + fragment.substr(7));
		}
	}
	else if(upperCaseFragment.startsWith("GROUP BY ")) {
		$("#stepExplanationText").text("Combine rows (according to the grouping function) with matching values in the following column(s): " + fragment.substr(9));
	}
	else if(upperCaseFragment.startsWith("ORDER BY ")) {
		fragment = fragment.substr(9);
		let columns = fragment.split(',');
		let column;
		let upperCaseColumn;
		for(let i=0; i<columns.length; i++) {
			column = columns[i].trim();
			upperCaseColumn = column.toUpperCase();
			if(upperCaseColumn.endsWith(" DESC")) {
				columns[i] = column.substr(0, column.length-5) + ' (highest to lowest)';
			} else if(upperCaseColumn.endsWith(" ASC")) {
				columns[i] = column.substr(0, column.length-4) + ' (lowest to highest)';
			} else {
				columns[i] = column + ' (lowest to highest)';
			}
		}
		let orderByExplanation = 'Sort all rows according to values in the following column(s): ';
		for(let i=0; i<columns.length; i++) {
			orderByExplanation += columns[i];
			if(i<columns.length-1) {
				orderByExplanation += ", then ";
			}
		}
		$("#stepExplanationText").text(orderByExplanation);
	}
	//joins
	else if(upperCaseFragment.startsWith("JOIN ") || upperCaseFragment.startsWith("LEFT ") || upperCaseFragment.startsWith("RIGHT ") || upperCaseFragment.startsWith("INNER ")) {
		let joinIndex = upperCaseFragment.indexOf("JOIN ");
		let onIndex = upperCaseFragment.indexOf("ON ");
		if(upperCaseFragment.startsWith("LEFT ")) {
			$("#stepExplanationText").text("Add columns from " + fragment.substring(joinIndex + 5, onIndex) + " that satisfy the following condition: " + fragment.substring(onIndex + 3) + " and retain any rows without corresponding column data from " + fragment.substring(joinIndex + 5, onIndex));
		} else {
			$("#stepExplanationText").text("Add columns from " + fragment.substring(joinIndex + 5, onIndex) + " that satisfy the following condition: " + fragment.substring(onIndex + 3) + " and remove any rows without corresponding column data from " + fragment.substring(joinIndex + 5, onIndex));
		}
	}
	//UNION or UNION ALL
	else if(upperCaseFragment.startsWith("UNION")  || upperCaseFragment.startsWith("UNION ALL")) {
	 	let finalString = "";
		 if(upperCaseFragment === "UNION") {
			 finalString = "Add all rows from the second query result set that aren't already in the first query result set.";
		 } else {
			 finalString = "Add all rows from the second query result set to the first query result set whether or not there are duplicate values."
		 }
		 $("#stepExplanationText").text(finalString);
	}

	else if(upperCaseFragment.startsWith("INTERSECT")) {
		let finalString = "Returns only the common rows between the results of two SELECT queries.";
		$("#stepExplanationText").text(finalString);
	}

	else if(upperCaseFragment.startsWith("EXCEPT")) {
		let finalString = "Retrieves all rows from the first query that are not present in the second query.";
		$("#stepExplanationText").text(finalString);
	}

	//where or having clause
	else {
		if(upperCaseFragment.startsWith("AND ")) {
			fragment = fragment.substr(4);
		} else if(upperCaseFragment.startsWith("OR ")) {
			fragment = fragment.substr(5);
		}
		$("#stepExplanationText").text("Remove all rows other than those that satisfy the following column condition: " + fragment);
	}
}

function highlightSelectWithoutGroupingFunctions(fragment, fragIndex, color) {
	fragIndex = updateHighlightedFragInd(fragIndex);
	var fromClauseListIndex = fragmentList.length-1;
	/*while(!fragmentList[fromClauseListIndex].toUpperCase().trim().startsWith("FROM")) {
		fromClauseListIndex--;
	}*/
	var nSelect = 0;
	while (fromClauseListIndex >= 0) {
		if (fragmentList[fromClauseListIndex].toUpperCase().trim().startsWith("SELECT")) {
			nSelect++;
		}
		else if (fragmentList[fromClauseListIndex].toUpperCase().trim().startsWith("FROM")) {
			nSelect--;
			if (nSelect <= 0) {
				break;
			}
		}		
		fromClauseListIndex--;
	}
	var fromClauseIndex = updateHighlightedFragInd(fragmentIndexList[fromClauseListIndex]);
	var selectClause = htmlQuery.substr(fragIndex, fromClauseIndex);
	var fromColoring = "";
	if(selectClause.endsWith("<span style='background-color:#0ACBEE'>")) {
		fromColoring = "<span style='background-color:#0ACBEE'>";
	}

	//remove existing yellow span tags from select (if present)
	while(selectClause != selectClause.replace("<span style='background-color:yellow'>", "")) {
		selectClause = selectClause.replace("<span style='background-color:yellow'>", "");
	}
	
	while(selectClause != selectClause.replace("<span style='background-color:#0ACBEE'>", "")) {
		selectClause = selectClause.replace("<span style='background-color:#0ACBEE'>", "");
	}

	while(selectClause != selectClause.replace("</span>", "")) {
		selectClause = selectClause.replace("</span>", "");
	}

	htmlQuery = htmlQuery.substr(0,fragIndex)+highlightSelectWithoutFunctions(selectClause, color)+fromColoring+htmlQuery.substr(fromClauseIndex);
}

function highlightGroupingFunctionsInSelect(color) {
	//find from clause index
	var fromClauseListIndex = fragmentList.length-1;
	while(!fragmentList[fromClauseListIndex].toUpperCase().trim().startsWith("FROM")) {
		fromClauseListIndex--;
	}
	var fromClauseIndex = updateHighlightedFragInd(fragmentIndexList[fromClauseListIndex]);

	//find select clause index
	var selectClauseListIndex = fragmentList.length-1;
	while(!fragmentList[selectClauseListIndex].toUpperCase().trim().startsWith("SELECT")) {
		selectClauseListIndex--;
	}
	var selectClauseIndex = updateHighlightedFragInd(fragmentIndexList[selectClauseListIndex]);
	var selectClause = htmlQuery.substring(selectClauseIndex, fromClauseIndex);

	htmlQuery = htmlQuery.substring(0,selectClauseIndex)+highlightSelectForGroupBy(selectClause, color)+htmlQuery.substr(fromClauseIndex);
}

function highlightGroupByWithGroupingFunctions(fragment, fragIndex, color) {
	//highlight GROUP BY
	fragIndex = updateHighlightedFragInd(fragIndex);
	htmlQuery = htmlQuery.substr(0,fragIndex)+"<span style='background-color:"+color+"'>"+fragment+"</span>"+htmlQuery.substr(fragment.length + fragIndex);

	//highlight grouping functions in SELECT
	highlightGroupingFunctionsInSelect(color);
}

function highlightSelectWithoutFunctions(selectClause, color) {
	var selectClauseHTML = "<span style='background-color:"+color+"'>";
	selectClauseHTML += selectClause.replace(/\s*AVG\s*\([^)]*\)/gi, unhighlightGroupingFunction);
	selectClauseHTML = selectClauseHTML.replace(/\s*COUNT\s*\([^)]*\)/gi, unhighlightGroupingFunction);
	selectClauseHTML = selectClauseHTML.replace(/\s*MAX\s*\([^)]*\)/gi, unhighlightGroupingFunction);
	selectClauseHTML = selectClauseHTML.replace(/\s*MIN\s*\([^)]*\)/gi, unhighlightGroupingFunction);
	selectClauseHTML = selectClauseHTML.replace(/\s*SUM\s*\([^)]*\)/gi, unhighlightGroupingFunction);
	//insert color
	while(selectClauseHTML != selectClauseHTML.replace("<span style='background-color:'>", "<span style='background-color:"+color+"'>")) {
		selectClauseHTML = selectClauseHTML.replace("<span style='background-color:'>", "<span style='background-color:"+color+"'>");
	}
	//close span if end of select isn't already coloring from blue
	
	if(selectClauseHTML.trim().endsWith("<span style='background-color:"+color+"'>")) {
		//selectClauseHTML += "</span>";
		var cut = "<span style='background-color:"+color+"'>";
		var inde = selectClauseHTML.lastIndexOf(cut);
		selectClauseHTML = selectClauseHTML.substr(0,inde) + selectClauseHTML.substr(inde+cut.length);
	}
	
	else {
		selectClauseHTML += "</span>";
	}
	
	return selectClauseHTML;
}

function highlightSelectForGroupBy(selectClause, color) {
	var selectClauseHTML = selectClause.replace(/\s*AVG\s*\([^)]*\)/gi, highlightGroupingFunction);
	selectClauseHTML = selectClauseHTML.replace(/\s*COUNT\s*\([^)]*\)/gi, highlightGroupingFunction);
	selectClauseHTML = selectClauseHTML.replace(/\s*MAX\s*\([^)]*\)/gi, highlightGroupingFunction);
	selectClauseHTML = selectClauseHTML.replace(/\s*MIN\s*\([^)]*\)/gi, highlightGroupingFunction);
	selectClauseHTML = selectClauseHTML.replace(/\s*SUM\s*\([^)]*\)/gi, highlightGroupingFunction);
	//insert color
	while(selectClauseHTML != selectClauseHTML.replace("<span style='background-color:'>", "<span style='background-color:"+color+"'>")) {
		selectClauseHTML = selectClauseHTML.replace("<span style='background-color:'>", "<span style='background-color:"+color+"'>");
	}
	return selectClauseHTML;
}

function unhighlightGroupingFunction(match) {
	var replacement = "</span>";
	replacement += match.substring(0, match.indexOf('(')+1);
	replacement += "<span style='background-color:'>" + match.substring(match.indexOf('(')+1, match.indexOf(')'))+"</span>";
	replacement += match.substring(match.indexOf(')'))+"<span style='background-color:'>";
	return replacement;
}

function highlightGroupingFunction(match) {
	return " <span style='background-color:'>" + match.trim() + "</span>";
}

function highlightEitherWhereOrHavingKeyword(fragment, fragIndex, color) {
	var numParenthesizedExpressions = 0;
	var updatedFragIndex = updateHighlightedFragInd(fragIndex);

	for(var i=updatedFragIndex; i>=0; i--) {
		if(htmlQuery.charAt(i) == ')') {
			numParenthesizedExpressions++;
		} else if(htmlQuery.charAt(i) == '(') {
			numParenthesizedExpressions--;
		} else if(numParenthesizedExpressions <= 0) {
			if(htmlQuery.substring(i, i+5).toUpperCase() == 'WHERE') {
				highlightWhere(fragment, fragIndex, color);
				return;
			} else if(htmlQuery.substring(i, i+6).toUpperCase() == 'HAVING') {
				highlightHaving(fragment, fragIndex, color);
				return;
			}
		}
	}
}

function highlightWhereOrHaving(fragment, fragIndex, color) {
	highlightEitherWhereOrHavingKeyword(fragment, fragIndex, color);
	fragIndex = updateHighlightedFragInd(fragIndex);
	
	//if the fragment was either:
	//	reordered and now begins with an 'and' and 'or'
	//	or contains HTML from subquery processing
	if(!htmlQuery.substr(fragIndex, fragIndex.length).startsWith(fragment)) {
		//if the fragment begins with 'OR'
		if(fragment.toUpperCase().trim().startsWith("OR")) {
			//if or was added before condition
			if (!htmlQuery.substr(fragIndex, 2).toUpperCase().startsWith("OR")) {
				var updatedFragment = fragment.replace(/or/gi, '');
				var orString = fragment.substring(0, fragment.indexOf(updatedFragment));
				updatedFragment = updatedFragment.replace(/\n/g, '<br>');
				
				// prevent fragment from shifting down lines
				// this is the spacing after the or
				var pattern = /( |<br>)+/;
				var offset = 0;
				var match = pattern.exec(updatedFragment);
				if (match != null && match.index == 0) {
					updatedFragment = updatedFragment.substr(match[0].length);
				}
				
				// get spacing before the or
				pattern = /( |<br>)+/g;
				var spacing = "";
				match = null;
				while ((match = pattern.exec(htmlQuery)) != null) {
					if (match.index == htmlQuery.indexOf(updatedFragment)+updatedFragment.length) {
						spacing = match[0];
						break;
					}
				}
				
				if (spacing.length == 0) {
					spacing = " ";
				}
				
				htmlQuery = htmlQuery.substr(0,fragIndex+offset)+"<span style='background-color:"+color+"'>"+ updatedFragment + spacing + orString + "</span>"+htmlQuery.substr(updatedFragment.length + orString.length + spacing.length + fragIndex + offset);

			} else {
				var updatedFragment = fragment.replace(/\n/g, '<br>');
				htmlQuery = htmlQuery.substr(0,fragIndex)+"<span style='background-color:"+color+"'>"+updatedFragment+"</span>"+htmlQuery.substr(updatedFragment.length + fragIndex);
			}
		} 
		//if the fragment begins with 'AND'
		else if(fragment.toUpperCase().trim().startsWith("AND")) {
			var updatedFragment = fragment.replace(/\n/g, '<br>');
			htmlQuery = htmlQuery.substr(0,fragIndex)+"<span style='background-color:"+color+"'>"+updatedFragment+"</span>"+htmlQuery.substr(updatedFragment.length + fragIndex);
			/*var andString = fragment.substring(0, fragment.indexOf(updatedFragment));
			//see if fragment begins with a newline
			if(htmlQuery.substr(htmlQuery.indexOf(fragment)-4, 4) == '<br>') {
				htmlQuery = htmlQuery.substr(0,fragIndex-1)+"<span style='background-color:"+color+"'>"+fragment+"</span>"+htmlQuery.substr(fragment.length + fragIndex - 1);
			} else {
				htmlQuery = htmlQuery.substr(0,fragIndex)+"<span style='background-color:"+color+"'>" + updatedFragment + ' ' + andString + "</span>"+htmlQuery.substr(updatedFragment.length + andString.length + 1 + fragIndex);
			}*/
		} 
		//the fragment contains HTML from subquery processing
		else {
			var updatedFragment = fragment.replace(/\n/g, '<br>');
			var fragmentEndIndex = updateHighlightedFragIndFromIndex(fragIndex, fragIndex+updatedFragment.length);
			//htmlQuery = htmlQuery.substr(0,fragIndex)+"<span style='background-color:"+color+"'>"+fragment+"</span>"+htmlQuery.substr(fragmentEndIndex);
			htmlQuery = htmlQuery.substr(0,fragIndex)+"<span style='background-color:"+color+"'>"+updatedFragment+"</span>"+htmlQuery.substr(fragmentEndIndex);//updatedFragment.length + fragIndex);
		}
	} else {
		var updatedFragment = fragment.replace(/\n/g, '<br>');
		htmlQuery = htmlQuery.substr(0,fragIndex)+"<span style='background-color:"+color+"'>"+updatedFragment+"</span>"+htmlQuery.substr(updatedFragment.length + fragIndex);
	}
}

function highlightWhere(fragment, fragIndex, color) {
	var numParenthesizedExpressions = 0;
	var updatedFragIndex = updateHighlightedFragInd(fragIndex);

	for(var i=updatedFragIndex; i>=0; i--) {
		if(htmlQuery.charAt(i) == ')') {
			numParenthesizedExpressions++;
		} else if(htmlQuery.charAt(i) == '(') {
			numParenthesizedExpressions--;
		} else if(numParenthesizedExpressions <= 0 && htmlQuery.substring(i, i+5).toUpperCase() == 'WHERE') {
			//if WHERE isn't already colored
			if(htmlQuery.substring(i-8, i-2).toUpperCase() != 'YELLOW' && htmlQuery.substring(i-9, i-2).toUpperCase() != '#0ACBEE') {
				htmlQuery = htmlQuery.substr(0,i)+"<span style='background-color:"+color+"'>"+htmlQuery.substring(i, i+5)+"</span>"+htmlQuery.substr(i+5, htmlQuery.length);
			}
			return;
		}
	}
}

function highlightHaving(fragment, fragIndex, color) {
	var numParenthesizedExpressions = 0;
	var updatedFragIndex = updateHighlightedFragInd(fragIndex);
	
	for(var i=updatedFragIndex; i>=0; i--) {
		if(htmlQuery.charAt(i) == ')') {
			numParenthesizedExpressions++;
		} else if(htmlQuery.charAt(i) == '(') {
			numParenthesizedExpressions--;
		} else if(numParenthesizedExpressions <= 0 && htmlQuery.substring(i, i+6).toUpperCase() == 'HAVING') {
			//if HAVING isn't already colored
			if(htmlQuery.substring(i-8, i-2).toUpperCase() != 'YELLOW' && htmlQuery.substring(i-9, i-2).toUpperCase() != '#0ACBEE') {
				htmlQuery = htmlQuery.substr(0,i)+"<span style='background-color:"+color+"'>"+htmlQuery.substring(i, i+6)+"</span>"+htmlQuery.substr(i+6, htmlQuery.length);
			}
			return;
		}
	}
}

function updateFragInd(fragIndex) {
	var numLineBreaksBeforeFragment = 0;
	var lineBreakInd = -1;
	
	//while no more line breaks before fragIndex are found
	do {
		lineBreakInd = query.indexOf('\n', lineBreakInd+1);
		if(lineBreakInd != -1 && lineBreakInd < fragIndex) {
			numLineBreaksBeforeFragment++;
		}
	} while(lineBreakInd != -1 && lineBreakInd < fragIndex);
	
	return fragIndex + 2*numLineBreaksBeforeFragment;
}

function updateHighlightedFragInd(fragIndex) {
	var yellowInd = -1;
	var blueInd = -1;
	var closeSpanInd = -1;
	var htmlInd = -1;
	var closingBraceInd = -1;
	
	do {
		//find html markup in html query
		yellowInd = htmlQuery.indexOf('<span style=\'background-color:yellow\'>', htmlInd+1);
		blueInd = htmlQuery.indexOf('<span style=\'background-color:#0ACBEE\'>', htmlInd+1);
		closeSpanInd = htmlQuery.indexOf('</span>', htmlInd+1);
		
		if(yellowInd != -1) {
			if(blueInd != -1) {
				if(closeSpanInd != -1) {
					//all are present
					htmlInd = Math.min(yellowInd, blueInd, closeSpanInd);
				} else {
					//yellow and blue are present
					htmlInd = Math.min(yellowInd, blueInd);
				}
			} else if(closeSpanInd != -1) {
				//yellow and close are present
				htmlInd = Math.min(yellowInd, closeSpanInd);
			} else {
				//only yellow is present (shouldn't happen)
				htmlInd = yellowInd;
			}
		} else if(blueInd != -1) {
			if(closeSpanInd != -1) {
				//blue and close are present
				htmlInd = Math.min(blueInd, closeSpanInd);
			} else {
				//only blue is present (shouldn't happen)
				htmlInd = blueInd;
			}
		} else if(closeSpanInd != -1) {
			//only close is present
			htmlInd = closeSpanInd;
		} else {
			//none are found
			break;
		}
		
		//if index of html markup is before fragIndex
		if(htmlInd != -1 && htmlInd <= fragIndex) {
			//add length of html markup additions to fragIndex
			closingBraceInd = htmlQuery.indexOf('>', htmlInd+1);
			fragIndex += closingBraceInd-htmlInd+1;
		}
	} while(htmlInd != -1 && htmlInd < fragIndex);

	return fragIndex;
}

function updateHighlightedFragIndFromIndex(startInd, endInd) {
	var yellowInd = -1;
	var blueInd = -1;
	var closeSpanInd = -1;
	var htmlInd = startInd-1;
	var closingBraceInd = -1;
	
	do {
		//find html markup in html query
		yellowInd = htmlQuery.indexOf('<span style=\'background-color:yellow\'>', htmlInd+1);
		blueInd = htmlQuery.indexOf('<span style=\'background-color:#0ACBEE\'>', htmlInd+1);
		closeSpanInd = htmlQuery.indexOf('</span>', htmlInd+1);
		
		if(yellowInd != -1) {
			if(blueInd != -1) {
				if(closeSpanInd != -1) {
					//all are present
					htmlInd = Math.min(yellowInd, blueInd, closeSpanInd);
				} else {
					//yellow and blue are present
					htmlInd = Math.min(yellowInd, blueInd);
				}
			} else if(closeSpanInd != -1) {
				//yellow and close are present
				htmlInd = Math.min(yellowInd, closeSpanInd);
			} else {
				//only yellow is present (shouldn't happen)
				htmlInd = yellowInd;
			}
		} else if(blueInd != -1) {
			if(closeSpanInd != -1) {
				//blue and close are present
				htmlInd = Math.min(blueInd, closeSpanInd);
			} else {
				//only blue is present (shouldn't happen)
				htmlInd = blueInd;
			}
		} else if(closeSpanInd != -1) {
			//only close is present
			htmlInd = closeSpanInd;
		} else {
			//none are found
			break;
		}
		
		//if index of html markup is before fragIndex
		if(htmlInd != -1 && htmlInd < endInd) {
			//add length of html markup additions to fragIndex
			closingBraceInd = htmlQuery.indexOf('>', htmlInd+1);
			endInd += closingBraceInd-htmlInd+1;
		}
	} while(htmlInd != -1 && htmlInd < endInd);
	
	return endInd;
}

function toggleHtmlQuery() {
	if(document.getElementById("inputTextArea") == null) {
		document.getElementById("parentDisplay").innerHTML='<textarea class="form-control" id="inputTextArea" rows="6" name="query" required>'+query+'</textarea>';
	}
}

function showTableData(table) {
	$("#"+table).show();
}

function closeSchemaTable(table) {
	$("#"+table).hide();
}
