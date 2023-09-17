<%@ page isELIgnored="false" contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>系统登录</title>

    <style>
        div{
            width:300px;
            height:100px;
            position: absolute;
            top:50%;
            left:50%;
            margin-top: -50px ;
            margin-left:-150px;
        }
    </style>
</head>
<body>

    <div>
        <form method="post" action="${pageContext.request.contextPath}/login/loginSystem">
            <table>
                <tr>
                    <td>用户名：</td>
                    <td><input type="text" name="username"></td>
                </tr>
                <tr>
                    <td>密码：</td>
                    <td><input type="password" name="password"> <input type="submit" value="登录"></td>
                </tr>
            </table>

        </form>
    </div>
</body>
</html>
