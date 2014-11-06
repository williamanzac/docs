package anzac.peripherals.help.model;

import static anzac.peripherals.help.HelpDoclet.processText;

public class ApiEvent extends ApiMethod {

	public String toSignature() {
		final StringBuilder builder = new StringBuilder();
		builder.append("\"").append(name).append("\"");
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
		if (!parameters.isEmpty()) {
			builder.append("\n\tArguments are: ");
			boolean first = true;
			for (final ApiParameter parameter : parameters) {
				if (!first) {
					builder.append(", ");
				}
				first = false;
				builder.append(parameter);
			}
		}
		return builder.toString();
	}
}
