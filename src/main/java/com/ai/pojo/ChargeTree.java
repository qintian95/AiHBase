package com.ai.pojo;

/**
 * @author tq
 * @date 2019/12/9 15:34
 */
public class ChargeTree {
    private String id;
    private String pre_id;
    private String case_name;
    private String case_type;
    private String case_hierarchy;

    public String getPre_id() {
        return pre_id;
    }

    public String getCase_name() {
        return case_name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {

        return id;
    }

    public void setPre_id(String pre_id) {
        this.pre_id = pre_id;
    }

    public void setCase_name(String case_name) {
        this.case_name = case_name;
    }

    public String getCase_type() {
        return case_type;
    }

    public void setCase_type(String case_type) {
        this.case_type = case_type;
    }

    public String getCase_hierarchy() {
        return case_hierarchy;
    }

    public void setCase_hierarchy(String case_hierarchy) {
        this.case_hierarchy = case_hierarchy;
    }
}
