package com.heima.freemarker.web;

import com.heima.freemarker.pojo.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/free")
public class FreeMarkerController {
    @GetMapping("/basic")
    public String test(Model model) {
        //1.纯文本形式的参数
        //model.addAttribute("name", "freemarker");
        //2.实体类相关的参数
        Student student = new Student();
        student.setName("小明");
        student.setAge(18);
        model.addAttribute("stu", student);
        return "01-basic";
    }
    @GetMapping("/list")
    public String test1(Model model) {
        //------------------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //小黑对象模型数据
        Student stu3 = new Student();
        stu3.setName("小黑");
        stu3.setMoney(200864.1f);
        stu3.setAge(22);
        stu3.setBirthday(new Date());

        //小蓝对象模型数据
        Student stu4 = new Student();
        stu4.setName("小蓝");
        stu4.setMoney(200864.1f);
        stu4.setAge(22);
        stu4.setBirthday(new Date());
        //创建List
        List<Student> list = new ArrayList<>();
        list.add(stu1);
        list.add(stu2);
        list.add(stu3);
        list.add(stu4);
        model.addAttribute("stus",list);
        return "02-list";
    }

    @GetMapping("/map")
    public String test2(Model model) {
        //------------------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //小黑对象模型数据
        Student stu3 = new Student();
        stu3.setName("小黑");
        stu3.setMoney(200864.1f);
        stu3.setAge(22);
        stu3.setBirthday(new Date());

        //小蓝对象模型数据
        Student stu4 = new Student();
        stu4.setName("小蓝");
        stu4.setMoney(200864.1f);
        stu4.setAge(22);
        stu4.setBirthday(new Date());
        //创建List
        List<Student> list = new ArrayList<>();
        list.add(stu1);
        list.add(stu2);
        list.add(stu3);
        list.add(stu4);
        //创建Map数据
        HashMap<String,Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
        stuMap.put("stu3",stu3);
        stuMap.put("stu4",stu4);
        // 3.1 向model中存放Map数据
        model.addAttribute("stuMap", stuMap);
        return "03-map";
    }

    @GetMapping("/if")
    public String test3(Model model) {
        //------------------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //小黑对象模型数据
        Student stu3 = new Student();
        stu3.setName("小黑");
        stu3.setMoney(200864.1f);
        stu3.setAge(22);
        stu3.setBirthday(new Date());

        //小蓝对象模型数据
        Student stu4 = new Student();
        stu4.setName("小蓝");
        stu4.setMoney(200864.1f);
        stu4.setAge(22);
        stu4.setBirthday(new Date());
        //创建List
        List<Student> list = new ArrayList<>();
        list.add(stu1);
        list.add(stu2);
        list.add(stu3);
        list.add(stu4);
        model.addAttribute("stus",list);
        return "04-if";
    }
}
