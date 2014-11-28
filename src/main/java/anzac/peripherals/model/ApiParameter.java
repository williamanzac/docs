package anzac.peripherals.model;

import com.sun.javadoc.Tag;

public class ApiParameter {
	public String name;
	public Tag[] description;
	public String type;

	@Override
	public String toString() {
		return name;
	}
}
