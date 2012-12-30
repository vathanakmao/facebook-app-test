<%@include file="header.jsp"%>

<div class="row">
    <label>User ID:</label> ${user.id} <br/>
    <label>Username:</label> ${user.username} <br/>
    <label>First Name:</label> ${user.first_name} <br/>
    <label>Last Name:</label> ${user.last_name} <br/>
    <label>Gender: </label> ${user.gender} </br>
    <label>Email: </label> ${user.email}
</div>

<%@include file="footer.jsp"%>