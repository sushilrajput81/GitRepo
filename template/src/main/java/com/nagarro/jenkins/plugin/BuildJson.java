package com.nagarro.jenkins.plugin;

public class BuildJson {
	private Builds[] builds;
	
	private LastBuild lastBuild;
	
	public Builds[] getBuilds ()
    {
        return builds;
    }

    public void setBuilds (Builds[] builds)
    {
        this.builds = builds;
    }
    
    public LastBuild getLastBuild() {
		return lastBuild;
	}

	public void setLastBuild(LastBuild lastBuild) {
		this.lastBuild = lastBuild;
	}

	public class Builds {
    	private String number;
    	
    	public String getNumber ()
        {
            return number;
        }

        public void setNumber (String number)
        {
            this.number = number;
        }
    }
    
    public class LastBuild {
    	private String number;
    	
    	public String getNumber ()
        {
            return number;
        }

        public void setNumber (String number)
        {
            this.number = number;
        }

    }
}
