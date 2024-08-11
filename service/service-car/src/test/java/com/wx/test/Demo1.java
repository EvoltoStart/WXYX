package com.wx.test;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
class User{
    private BigDecimal num;


}

public class Demo1 {
    public static void main(String[] args) {
        List<User> list=new ArrayList<>();
        User u1=new User();
        u1.setNum(BigDecimal.valueOf(100));
        User u2=new User();
        u2.setNum(BigDecimal.valueOf(200));
        list.add(u1);
        list.add(u2);
        //BigDecimal result=list.stream().map(User::getNum).reduce(BigDecimal::add).get();
        BigDecimal result=list.stream().map(User::getNum).reduce(BigDecimal.ZERO,BigDecimal::add);//list无数据为0

        System.out.println(result);

    }


}
