package com.sunit.ems.models;

/**
 * Leave POJO class with setters and getters.
 */
public class Leave {
  private   String ID,approvedBy,reason,endDate,startDate,timeStamp,designation,
    department,email,name,phone;
  private   int noOfDays,status;
    public Leave(){}

    public Leave(String ID,String approvedBy,String reason,String endDate,String startDate,
    String timeStamp,String name,String designation,String department,String email,String phone,int noOfDays,int status )
    {
        this.ID=ID;
        this.approvedBy=approvedBy;
        this.reason=reason;
        this.endDate=endDate;
        this.startDate=startDate;
        this.timeStamp=timeStamp;
        this.email=email;
        this.phone=phone;
        this.name=name;
        this.noOfDays=noOfDays;
        this.designation=designation;
        this.department=department;
        this.status=status;


    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getID() {
        return ID;
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    public String getStartDate() {
        return startDate;
    }



    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }



    public int getNoOfDays() {
        return noOfDays;
    }

    public void setNoOfDays(int noOfDays) {
        this.noOfDays = noOfDays;
    }
}
