package anzac.peripherals.docs.model;

import anzac.peripherals.docs.APIDoclet;

import com.sun.javadoc.Tag;

public class ParameterXML {
	private String name;
	private String type;
	private Tag[] description;

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

	public Tag[] getDescription() {
		return description;
	}

	public void setDescription(Tag[] description) {
		this.description = description;
	}

	public String toXML(final String className) {
		return "<dd><code>" + name + "</code> " + APIDoclet.processText(className, description) + "</dd>";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParameterXML other = (ParameterXML) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
