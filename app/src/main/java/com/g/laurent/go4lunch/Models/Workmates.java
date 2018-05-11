package com.g.laurent.go4lunch.Models;

public class Workmates {

    private String name;
    private Boolean chosen;
    private String resto_id;

    public Workmates(String name, Boolean chosen, String resto_id) {
        this.name = name;
        this.chosen = chosen;
        this.resto_id = resto_id;
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
}
