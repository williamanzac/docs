package anzac.peripherals.help.model;

import static anzac.peripherals.help.HelpDoclet.processText;

import java.util.ArrayList;
import java.util.List;

import com.sun.javadoc.Tag;

public class ApiMethod implements Comparable<ApiMethod> {
	public String name;
	public Tag[] description;
	public String returnType;
	public List<ApiParameter> parameters = new ArrayList<ApiParameter>();

	public String toSignature() {
		final StringBuilder builder = new StringBuilder();
		builder.append(name).append("(");
		if (!parameters.isEmpty()) {
			boolean first = true;
			for (final ApiParameter parameter : parameters) {
				if (!first) {
					builder.append(", ");
				}
				first = false;
				builder.append(parameter);
			}
		}
		builder.append(")");
		return builder.toString();
	}

	public String toHelp(final String className) {
		final StringBuilder builder = new StringBuilder();
		if (returnType != null && !returnType.isEmpty()) {
			builder.append(returnType).append(" ");
		}
		builder.append(toSignature());
		if (description != null) {
			builder.append(" ").append(processText(className, description));
		}
		for (final ApiParameter parameter : parameters) {
			builder.append("\n\t").append(parameter.toHelp(className));
		}
		return builder.toString();
	}

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
