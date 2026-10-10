package com.amigos.attendance.models;

public class EmployeeModel {
    private String emp_id;
    private String emp_name;
    private String currentDateTime;

    public EmployeeModel() {
    }

    public EmployeeModel(String emp_id, String currentDateTime, String emp_name) {
        this.emp_id = emp_id;
        this.currentDateTime = currentDateTime;
        this.emp_name = emp_name;
    }

    public String getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(String emp_id) {
        this.emp_id = emp_id;
    }

    public String getEmp_name() {
        return emp_name;
    }

    public void setEmp_name(String emp_name) {
        this.emp_name = emp_name;
    }

    public String getCurrentDateTime() {
        return currentDateTime;
    }

    public void setCurrentDateTime(String currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    @Override
    public String toString() {
        return "EmployeeModel{" +
                "emp_id='" + emp_id + '\'' +
                ", emp_name='" + emp_name + '\'' +
                ", currentDateTime='" + currentDateTime + '\'' +
                '}';
    }
}
