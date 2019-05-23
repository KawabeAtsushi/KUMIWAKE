package com.pandatone.kumiwake.member;

import java.io.Serializable;

/**
 * Created by atsushi_2 on 2016/03/02.
 */
public class Name implements Serializable{
    protected int id;
    protected String name;
    protected String name_read;
    protected String  sex;
    protected int age;
    protected int grade;
    protected String belong;
    protected String role;

    public Name(int id, String name, String  sex,int age, int grade,String belong,String role,String name_read) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.grade = grade;
        this.belong = belong;
        this.role = role;
        this.name_read = name_read;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj != null && obj instanceof Name ){
            final Name target = (Name)obj;

            return ( target.id == this.id );
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }



    public int getId(){return id;}

    public String getName() {return name;}

    public String getName_read() {return name_read;}

    public String getSex(){return sex;}

    public int getAge(){return age;}

    public int getGrade(){return grade;}

    public String getBelong(){return belong;}

    public String getRole(){return role;}

}