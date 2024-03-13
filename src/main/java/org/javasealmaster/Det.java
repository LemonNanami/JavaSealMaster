package org.javasealmaster;

public class Det {
    private String cls;
    private Box box;

    public Det(String cls, Box box) {
        this.cls = cls;
        this.box = box;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public Box getBox() {
            return box;
    }

    public void setBox(Box box) {
        this.box = box;
    }

    }