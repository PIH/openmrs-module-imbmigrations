<%@ taglib prefix="c" uri="/WEB-INF/taglibs/c-rt.tld" %>
<%@ include file="/WEB-INF/template/include.jsp"%>

<div id="page">
	<div id="container">
		<h1><spring:message code="imbmigrations.title"/></h1>

		<h3>Complex Obs Migrator for value_complex that is the same across several Obs</h3>
		<br/>
		<c:forEach items="${migrator.obsWithSameValueComplex}" var="valueEntry">
			<h4>${valueEntry.key}</h4>
			<c:forEach items="${valueEntry.value}" var="obsRef">
				<form method="post" action="migrateComplexObsWithSameValueComplex.form">
					<input type="hidden" name="uuid" value="${obsRef.obsUuid}">
					<table>
						<tr>
							<td>
								<a href="/patientDashboard.form?patientId=${obsRef.patientId}" target="_blank">View Patient ${obsRef.patientId}</a>
							</td>
							<td>
								${obsRef.encounterDate}
							</td>
							<td>
								<a href="/complexObsServlet?obsId=${obsRef.obsId}" target="_blank">View File</a>
							</td>
							<td><input type="submit" value="Choose this Obs"/></td>
						</tr>
					</table>
				</form>
			</c:forEach>
			<hr/>
		</c:forEach>

	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
