package anzac.peripherals.docs.model;

public class ParameterXML {
	private String name;
	private String type;
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public String getDescription() {
		return description == null ? "" : description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toXML() {
		return "<dd><code>" + name + "</code>" + description + "</dd>";
	}
}
