$(document).ready(function() {
    $("#requireAuthentication").click(function () {
        if ($(this).prop("checked") == true) {
            $("#passwordFields").show();
        } else {
            $("#passwordFields").hide();
        }
    });
});

function validateAddUser() {
    if($("#email").val() === "") {
        $("#errors").html("Please provide an email address");
        $("#errors").show();
        return false;
    }
    if($("#requireAuthentication").is(":checked")) {
        if($("#password").val() === "") {
            $("#errors").html("Please provide a password");
            $("#errors").show();
            return false;
        }
        if($("#verifyPassword").val() === "") {
            $("#errors").html("Please validate your password");
            $("#errors").show();
            return false;
        }
        if($("#password").val() != $("#verifyPassword").val()) {
            $("#errors").html("Your passwords do not match");
            $("#errors").show();
            return false;
        }
    }
    return true;
}