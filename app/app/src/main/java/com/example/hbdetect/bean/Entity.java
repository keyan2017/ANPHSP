package com.example.hbdetect.bean;

public class Entity {
//    public static final String CREATE_ENTITY_TABLE = "create table EntityList ("
//            + "id integer primary key autoincrement, "
//            + "mchc text,"
//            + "mhch_real text,"
//            + "image blob,"
//            + "time text,"
//            + "take int,"
//            + "case_id text)";
    private byte[] image;

    //2024-1-11新建左右眼字段
    private String eye_side;

    public String getEye_side() {
        return eye_side;
    }

    public void setEye_side(String eye_side) {
        this.eye_side = eye_side;
    }

    public Entity(byte[] image, String mchc, String mchc_real, String case_id, String time, int take,String eye_side) {
        this.image = image;
        this.mchc = mchc;
        this.mchc_real = mchc_real;
        this.case_id = case_id;
        this.time = time;
        this.take = take;
        this.eye_side=eye_side;
    }

    private String mchc;
    private String mchc_real;
    private String case_id;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTake() {
        return take;
    }

    public void setTake(int take) {
        this.take = take;
    }

    private int take;

    public String getMchc() {
        return mchc;
    }

    public String getMchc_real() {
        return mchc_real;
    }

    public String getCase_id() {
        return case_id;
    }

    public void setMchc(String mchc) {
        this.mchc = mchc;
    }

    public void setMchc_real(String mchc_real) {
        this.mchc_real = mchc_real;
    }

    public void setCase_id(String case_id) {
        this.case_id = case_id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }


}
