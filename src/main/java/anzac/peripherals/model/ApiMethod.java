package anzac.peripherals.model;

import java.util.ArrayList;
import java.util.List;

import com.sun.javadoc.Tag;

public class ApiMethod implements Comparable<ApiMethod> {
	public String name;
	public Tag[] description;
	public String returnType;
	public List<ApiParameter> parameters = new ArrayList<ApiParameter>();
	public Tag[] firstLine;
	public Tag[] returnDescription;

	@Override
	public int compareTo(final ApiMethod that) {
		final int compareTo = this.name.compareTo(that.name);
		if (compareTo > 0) {
			return 1;
		} else if (compareTo < 0) {
			return -1;
		}
		if (this.parameters.size() < that.parameters.size()) {
			return -1;
		} else if (this.parameters.size() > that.parameters.size()) {
			return 1;
		}
		return 0;
	}
}
