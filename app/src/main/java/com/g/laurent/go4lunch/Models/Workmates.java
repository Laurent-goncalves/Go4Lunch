package com.g.laurent.go4lunch.Models;

public class Workmates {

    private String name;
    private String id;
    private Boolean chosen;
    private String resto_id;
    private String resto_type;
    private String resto_name;

    public Workmates(String name, String id, Boolean chosen, String resto_id, String resto_name, String resto_type) {
        this.name = name;
        this.id=id;
        this.chosen = chosen;
        this.resto_id = resto_id;
        this.resto_type=resto_type;
        this.resto_name=resto_name;
    }

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

    public Boolean getChosen() {
        return chosen;
    }

    public void setChosen(Boolean chosen) {
        this.chosen = chosen;
    }

    public String getResto_id() {
        return resto_id;
    }

    public void setResto_id(String resto_id) {
        this.resto_id = resto_id;
    }

    public String getResto_type() {
        return resto_type;
    }

    public void setResto_type(String resto_type) {
        this.resto_type = resto_type;
    }

    public String getResto_name() {
        return resto_name;
    }

    public void setResto_name(String resto_name) {
        this.resto_name = resto_name;
    }
}
