package anzac.peripherals.help.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ApiClass {
	public String type;
	public String description;
	public final Set<ApiMethod> methods = new TreeSet<ApiMethod>();
	public final List<ApiEvent> events = new ArrayList<ApiEvent>();

	public String toHelp() {
		final StringBuilder builder = new StringBuilder();
		builder.append(description).append('\n');
		builder.append("Functions in the " + type + " API:\n");
		for (final ApiMethod method : methods) {
			builder.append(method.toHelp()).append('\n');
		}
		builder.append("Events fired by the " + type + " API:\n");
		for (final ApiEvent event : events) {
			builder.append(event.toHelp()).append('\n');
		}
		return builder.toString();
	}
}
