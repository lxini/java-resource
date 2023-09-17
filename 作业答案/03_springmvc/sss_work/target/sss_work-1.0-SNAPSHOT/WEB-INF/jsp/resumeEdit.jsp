<%@ page isELIgnored="false" contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt"%>
<html>
<head>
    <title>简历详情页</title>
</head>
<body>
    <form method="post" action="${pageContext.request.contextPath}/resume/updateResume">
        <table border="1" width="100%">
            <input type="hidden" name="id" value="${resume.id}"></input>
            <tr>
                <td>姓名</td>
                <td><input type="text" name="name" value="${resume.name}"/></td>
            </tr>
            <tr>
                <td>地址</td>
                <td><input type="text" name="address" value="${resume.address}"/></td>
            </tr>
            <tr>
                <td>电话</td>
                <td><input type="text" name="phone" value="${resume.phone}"/></td>
            </tr>
        </table>
        <input type="submit" value="提交数据"/>
    </form>

</body>
</html>
