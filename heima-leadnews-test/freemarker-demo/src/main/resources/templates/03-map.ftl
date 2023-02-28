<!DOCTYPE html>
<html lang="utf-8">
<head>
    <meta charset="utf-8">
    <title>Hello freeWorker list</title>
</head>
<body>
<#--Map数据展示-->
<b>map数据的展示:</b>
<br/><br/>
<a href="###">方式一：通过map['keyname'].property</a><br/>
输出stu1的学生信息：<br/>
姓名：${stuMap['stu1'].name}<br/>
年龄：${stuMap['stu1'].age}<br/>
<br/>
<a href="###">方式二：通过map.keyname.property</a><br/>
输出stu2的学生信息：<br/>
姓名：${stuMap.stu2.name}<br/>
年龄：${stuMap.stu2.age}<br/>
<a href="###">方式一：通过map['keyname'].property</a><br/>
输出stu3的学生信息：<br/>
姓名：${stuMap['stu3'].name}<br/>
年龄：${stuMap['stu3'].age}<br/>
<br/>
<a href="###">方式二：通过map.keyname.property</a><br/>
输出stu4的学生信息：<br/>
姓名：${stuMap.stu4.name}<br/>
年龄：${stuMap.stu4.age}<br/>

<br/>
<a href="###">遍历map的数据</a>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#list stuMap?keys as key>
        <tr>
            <td>${key_index+1}</td>
            <td>${stuMap[key].name}</td>
            <td>${stuMap[key].age}</td>
            <td>${stuMap[key].money}</td>
        </tr>
    </#list>
</table>
</body>
</html>