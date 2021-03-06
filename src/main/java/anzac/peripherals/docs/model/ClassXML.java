package anzac.peripherals.docs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.text.StrBuilder;

public class ClassXML {
	private String name;
	private String fullName;
	private String type;
	private final Set<MethodXML> methods = new TreeSet<MethodXML>();
	private final List<EventXML> events = new ArrayList<EventXML>();

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Set<MethodXML> getMethods() {
		return methods;
	}

	public List<EventXML> getEvents() {
		return events;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	public String toXML() {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<section id=\"api\">");
		builder.appendln("<h2>API</h2>");
		builder.appendln("<dl>");
		builder.appendln("<dt>Peripheral Type</dt>");
		builder.appendln("<dd>" + type + "</dd>");
		builder.appendln("</dl>");
		builder.appendln("</section>");
		builder.appendln("<section id=\"summary\">");
		builder.appendln("<h3>Method Summary</h3>");
		builder.appendln("<table class=\"table\">");
		builder.appendln("<thead>");
		builder.appendln("<tr>");
		builder.appendln("<th>Return Type</th>");
		builder.appendln("<th>Method and Description</th>");
		builder.appendln("</tr>");
		builder.appendln("</thead>");
		builder.appendln("<tbody>");
		for (final MethodXML methodXML : methods) {
			if (methodXML.isPeripheralMethod()) {
				builder.append(methodXML.toSummaryXML(name));
			}
		}
		builder.appendln("</tbody>");
		builder.appendln("</table>");
		if (!events.isEmpty()) {
			builder.appendln("<h3>Event Summary</h3>");
			builder.appendln("<ul>");
			for (final EventXML event : events) {
				builder.append(event.toSummaryXML());
			}
			builder.appendln("</ul>");
		}
		builder.appendln("</section>");
		builder.appendln("<section id=\"detail\">");
		builder.appendln("<h3>Method Detail</h3>");
		for (final MethodXML methodXML : methods) {
			if (methodXML.isPeripheralMethod()) {
				builder.append(methodXML.toDetailXML(name));
			}
		}

		if (!events.isEmpty()) {
			builder.appendln("<h3>Event Detail</h3>");
			for (final EventXML event : events) {
				builder.append(event.toDetailXML(name));
			}
		}
		builder.appendln("</section>");
		return builder.toString();
	}
}
