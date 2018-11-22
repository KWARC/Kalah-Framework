package engine.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CmapDefinition {

	public enum CmapType{
		UNIFORM("uniform"),
		DIVERGENT("divergent");
		
		private String alias;
		
		private CmapType(String alias) {
			this.alias = alias;
		}
		
		public String asString() {
			return alias;
		}
	};
	
	@JsonProperty
	private String type;
	
	@JsonProperty
	private String good;
	
	@JsonProperty
	private String neutral;
	
	@JsonProperty
	private String bad;
	
	@JsonProperty
	private String color;
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	private double lower;
	
	@JsonProperty
	private double upper;

	public double getLower() {
		return lower;
	}

	public void setLower(double lower) {
		this.lower = lower;
	}

	public double getUpper() {
		return upper;
	}

	public void setUpper(double upper) {
		this.upper = upper;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGood() {
		return good;
	}

	public void setGood(String good) {
		this.good = good;
	}

	public String getNeutral() {
		return neutral;
	}

	public void setNeutral(String neutral) {
		this.neutral = neutral;
	}

	public String getBad() {
		return bad;
	}

	public void setBad(String bad) {
		this.bad = bad;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
