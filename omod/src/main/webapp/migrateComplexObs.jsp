<%@ taglib prefix="c" uri="/WEB-INF/taglibs/c-rt.tld" %>
<%@ include file="/WEB-INF/template/include.jsp"%>

<div id="page">
	<div id="container">
		<h1><spring:message code="imbmigrations.title"/></h1>

		<h3>Complex Obs Migrator</h3>
		<table>
		<c:forEach items="${migrator.migrationStatus}" var="statusEntry">
			<tr><td>${statusEntry.key}:</td><td>${statusEntry.value}</td></tr>
		</c:forEach>
		</table>
		<br/>
		<form method="post" action="migrateAllObs.form">
			<input type="submit" value="Execute Bulk Migration"/>
		</form>
		<br/>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
