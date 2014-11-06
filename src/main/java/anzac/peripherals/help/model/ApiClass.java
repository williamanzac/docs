package anzac.peripherals.help.model;

import static anzac.peripherals.help.HelpDoclet.processText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.javadoc.Tag;

public class ApiClass {
	public String name;
	public String type;
	public Tag[] description;
	public final Set<ApiMethod> methods = new TreeSet<ApiMethod>();
	public final List<ApiEvent> events = new ArrayList<ApiEvent>();

	public String toHelp() {
		final StringBuilder builder = new StringBuilder();
		builder.append(processText(name, description)).append('\n');
		if (!methods.isEmpty()) {
			builder.append("Methods exposed by " + type + " peripherals:\n");
			for (final ApiMethod method : methods) {
				builder.append(method.toHelp(name)).append('\n');
			}
		}
		if (!events.isEmpty()) {
			builder.append("Events fired by " + type + " peripherals:\n");
			for (final ApiEvent event : events) {
				builder.append(event.toHelp(name)).append('\n');
			}
		}
		return builder.toString();
	}
}
