package ru.tinkoff.eclair.logger;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Viacheslav Klapatniuk
 */
@XmlRootElement
@XmlType(name = "dto")
@Getter
@Setter
public class Dto {

    private int i;
    private String s;

    @Override
    public String toString() {
        return "Dto{" +
                "i=" + i +
                ", s='" + s + '\'' +
                '}';
    }
}
