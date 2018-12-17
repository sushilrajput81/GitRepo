package com.nagarro.jenkins.plugin.jenkinsJobsTemplates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * For Performance plugin in jenkins.xml
 * @author saurabh01
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="performance-run")
public class PerformancePlugin {
	
	@XmlElement(name="property")
    private Property[] property;
	
	public Property[] getProperty () {
        return property;
    }

    public void setProperty (Property[] property) {
        this.property = property;
    }
}
