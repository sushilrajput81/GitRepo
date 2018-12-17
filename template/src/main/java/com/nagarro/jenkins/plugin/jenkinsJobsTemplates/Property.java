package com.nagarro.jenkins.plugin.jenkinsJobsTemplates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="property")
public class Property {
	
	@XmlElement(name="name")
	private String name;

	@XmlElement(name="value")
    private String[] value;
    
//	@XmlElement(name="")
//    private Values values;

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String[] getValue () {
        return value;
    }

    public void setValue (String[] value) {
        this.value = value;
    }

	/*public Values getValues() {
		return values;
	}

	public void setValues(Values values) {
		this.values = values;
	}*/

}
