<!-- Navigation-->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark" id="mainNav">
    <a class="navbar-brand" href="/dbqa"><i class="fas fa-database" style="color:#FFFFFF"></i> DBQA</a>
    <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarResponsive">
        <ul class="navbar-nav ml-auto">
            <c:if test="${sessionScope.user != null}">
                <li class="nav-item">
                    <a class="nav-link" href="/dbqa/query"><i class="fas fa-user"></i></a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/dbqa/logout"><i class="fas fa-sign-out-alt"></i> Logout</a>
                </li>
            </c:if>
            <c:if test="${sessionScope.user == null}">
                <li class="nav-item">
                    <a class="nav-link" href="/dbqa/login"><i class="fas fa-sign-in-alt"></i> Login</a>
                </li>
            </c:if>
        </ul>
    </div>
</nav>