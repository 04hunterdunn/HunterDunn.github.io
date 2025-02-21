$(document).ready(function() {
    $("#usernameAsSchema").click(function () {
        if ($(this).prop("checked") == true) {
            $("#schemaFormGroup").hide();
            $("#schema").val("");
        } else {
            $("#schemaFormGroup").show();
        }
    });
});

function validateAddDatabaseType() {
    if($("#name").val() === "") {
        $("#errors").html("Please provide a database name");
        $("#errors").show();
        return false;
    }
    if($("#driver").val() === "") {
        $("#errors").html("Please provide a database driver class name");
        $("#errors").show();
        return false;
    }
    if($("#dialect").val() === "") {
        $("#errors").html("Please provide a hibernate dialect class name");
        $("#errors").show();
        return false;
    }
    return true;
}