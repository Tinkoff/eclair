package ru.tinkoff.integration.eclair.example;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name = "dto")
public class Dto {

    private int i;
    private String s;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return "Dto{" +
                "i=" + i +
                ", s='" + s + '\'' +
                '}';
    }
}
