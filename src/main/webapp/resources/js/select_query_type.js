function validateSelection() {
    if(document.itemSelectionForm.queryType.value === "") {
        alert("Please select a query type");
        return false;
    }
    return true;
}