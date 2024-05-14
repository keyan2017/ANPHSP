package com.example.hbdetect.bean;

public class Patient {

//    public String toString() {
//        return "Patient{" +
//                "id='" + id + '\'' +
//                ", name='" + name + '\'' +
//                ", age='" + age + '\'' +
//                ", gender='" + gender + '\'' +
//                ", deparments='" + deparments + '\'' +
//                ", bed_id='" + bed_id + '\'' +
//                ", case_id='" + case_id + '\'' +
//                '}';
//    }
    private String id;

    public Patient(String id, String name, String age, String gender, String deparments, String bed_id, String case_id) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;//性别
        this.deparments = deparments;//科室
        this.bed_id = bed_id;//床号
        this.case_id = case_id;
    }

    private String name;
    private String age;
    private String gender;
    private String deparments;
    private String bed_id;
    private String case_id;





    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDeparments() {
        return deparments;
    }

    public void setDeparments(String deparments) {
        this.deparments = deparments;
    }

    public String getBed_id() {
        return bed_id;
    }

    public void setBed_id(String bed_id) {
        this.bed_id = bed_id;
    }

    public String getCase_id() {
        return case_id;
    }

    public void setCase_id(String case_id) {
        this.case_id = case_id;
    }


}
