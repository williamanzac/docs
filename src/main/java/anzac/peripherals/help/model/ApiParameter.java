package anzac.peripherals.help.model;

import static anzac.peripherals.help.HelpDoclet.processText;

import com.sun.javadoc.Tag;

public class ApiParameter {
	public String name;
	public Tag[] description;
	public String type;

	@Override
	public String toString() {
		return name;
	}

	public String toHelp(final String className) {
		return type + " " + name + " " + processText(className, description);
	}
}
