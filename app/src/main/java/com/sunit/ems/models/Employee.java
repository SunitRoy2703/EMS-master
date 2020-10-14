package com.sunit.ems.models;

/**
 * Employee POJO class with setters and getters.
 */
public class Employee {
   private String id,name,email,phone,designation,department,photoUrl;
   private int leaves;
   private boolean isHr;


    public Employee(){}

    public Employee(String id,String photoUrl,String name,String email,String phone,String department,String designation,int leaves,boolean isHr)
    {
        this.id=id;
        this.name=name;
        this.email=email;
        this.designation=designation;
        this.phone=phone;
        this.department=department;
        this.leaves=leaves;
        this.isHr=isHr;
        this.photoUrl=photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setIsHr(boolean hr) {
        isHr = hr;
    }

    public boolean getIsHr() {
        return isHr;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public int getLeaves() {
        return leaves;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setLeaves(int leaves) {
        this.leaves = leaves;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
