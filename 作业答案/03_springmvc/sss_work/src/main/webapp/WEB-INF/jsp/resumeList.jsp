<%@ page isELIgnored="false" contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt"%>
<html>
<head>
    <a href="${pageContext.request.contextPath}/resume/resumeEdit" style="float:right;text-decoration:none;">新增</a>
</head>
<body>
    <input type="button" value="新增" onclick="">
    <table border="1" width="100%">
        <%--表头--%>
        <tr>
            <td>序号</td>
            <td>姓名</td>
            <td>地址</td>
            <td>电话</td>
            <td>操作</td>
        </tr>

        <c:forEach items="${resumeList}" var="resume" varStatus="s">
            <tr>
                <td>${s.count}</td>
                <td>${resume.name}</td>
                <td>${resume.address}</td>
                <td>${resume.phone}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/resume/resumeEdit?id=${resume.id}">修改</a>
                    <a href="${pageContext.request.contextPath}/resume/resumeDelete?id=${resume.id}">删除</a>
                </td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>
