function validateAddDatabase() {
    if($("#databaseTypeId").val() === "") {
        $("#errors").html("Please select a database type");
        $("#errors").show();
        return false;
    }
    if($("#name").val() === "") {
        $("#errors").html("Please provide a database name");
        $("#errors").show();
        return false;
    }
    if($("#url").val() === "") {
        $("#errors").html("Please provide a database url");
        $("#errors").show();
        return false;
    }
    return true;
}