function validateLogin() {
    if($("#email").val() === "") {
        $("#errors").html("Please enter your email address");
        $("#errors").show();
        return false;
    }
    if($("#password").val() === "") {
        $("#errors").html("Please enter your password");
        $("#errors").show();
        return false;
    }
    return true;
}