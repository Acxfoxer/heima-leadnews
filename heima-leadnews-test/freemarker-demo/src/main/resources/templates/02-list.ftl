<!DOCTYPE html>
<html lang="utf-8">
<head>
    <meta charset="utf-8">
    <title>Hello freeWorker list</title>
</head>
<body>
<b>展示list集合中的数据</b>
<br>
<br>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#list stus as stu>
        <#if stu.name='小红' >
            <tr STYLE="color: red">
                <td>${stu_index+1}</td>
                <td>${stu.name+1}</td>
                <td>${stu.age+1}</td>
                <td>${stu.money+1}</td>
            </tr>
        <#else>
            <tr>
                <td>${stu_index+1}</td>
                <td>${stu.name+1}</td>
                <td>${stu.age+1}</td>
                <td>${stu.money+1}</td>
            </tr>
        </#if>
    </#list>
</table>
<hr>

</body>
</html>