package anzac.peripherals.help;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import anzac.peripherals.Transformer;
import anzac.peripherals.model.ApiClass;
import anzac.peripherals.model.ApiEvent;
import anzac.peripherals.model.ApiMethod;
import anzac.peripherals.model.ApiParameter;
import anzac.peripherals.model.Block;
import anzac.peripherals.model.Item;
import anzac.peripherals.model.ModelGenerator;

import com.sun.javadoc.Tag;

public class HelpTransformer implements Transformer {

	public String transformClass(final ApiClass apiClass) {
		final StringBuilder builder = new StringBuilder();
		builder.append(processText(apiClass.name, apiClass.description)).append('\n');
		if (!apiClass.methods.isEmpty()) {
			builder.append("\nMethods exposed by " + apiClass.type + " peripherals:\n");
			for (final ApiMethod method : apiClass.methods) {
				builder.append(transformMethodDetail(method, apiClass.name)).append('\n');
			}
		}
		if (!apiClass.events.isEmpty()) {
			builder.append("\nEvents fired by " + apiClass.type + " peripherals:\n");
			for (final ApiEvent event : apiClass.events) {
				builder.append(transformEventDetail(event, apiClass.name)).append('\n');
			}
		}
		return builder.toString();
	}

	@Override
	public String transformParameter(final ApiParameter apiParameter, final String className) {
		return apiParameter.type + " " + apiParameter.name + " " + processText(className, apiParameter.description);
	}

	public String toSignature(final ApiMethod apiMethod) {
		final StringBuilder builder = new StringBuilder();
		builder.append(apiMethod.name).append("(");
		if (!apiMethod.parameters.isEmpty()) {
			boolean first = true;
			for (final ApiParameter parameter : apiMethod.parameters) {
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

	public String toSignature(final ApiEvent apiEvent) {
		final StringBuilder builder = new StringBuilder();
		builder.append("\"").append(apiEvent.name).append("\"");
		return builder.toString();
	}

	private static final Pattern methodPattern = Pattern.compile("(.*)#(.*)\\(");
	private static final Pattern eventPattern = Pattern.compile("(PeripheralEvent)#(.*)");

	private String processSig(final String name, final String text) {
		final StringBuilder builder = new StringBuilder();
		final Matcher methodMatcher = methodPattern.matcher(text);
		if (methodMatcher.find()) {
			// get Class
			final String strClass = methodMatcher.group(1);
			ApiClass apiClass;
			if (!strClass.isEmpty() && !name.equals(strClass)) {
				apiClass = ModelGenerator.apiClasses.get(strClass);
				if (apiClass == null) {
					builder.append(strClass);
					HelpDoclet.logWarning("could not find type for class " + strClass);
				} else {
					builder.append(apiClass.type);
				}
				builder.append(".");
			} else {
				apiClass = ModelGenerator.apiClasses.get(name);
			}
			// get method
			final String strMethod = methodMatcher.group(2);
			if (!strMethod.isEmpty()) {
				if (apiClass == null) {
					builder.append(strMethod);
				} else {
					for (final ApiMethod method : apiClass.methods) {
						if (method.name.equals(strMethod)) {
							builder.append(toSignature(method));
							break;
						}
					}
				}
			}
			return builder.toString();
		}
		final Matcher eventMatcher = eventPattern.matcher(text);
		if (eventMatcher.find()) {
			// get method
			builder.append(eventMatcher.group(2));
			return builder.toString();
		}
		final ApiClass apiClass = ModelGenerator.apiClasses.get(text);
		if (apiClass != null) {
			return apiClass.name;
		}
		return text;
	}

	public String processText(final String name, final Tag[] tags) {
		final StringBuilder strBuilder = new StringBuilder();
		if (tags != null) {
			for (final Tag tag : tags) {
				final String text = tag.text().trim();
				if ("Text".equals(tag.name())) {
					strBuilder.append(text);
				} else if ("@link".equals(tag.name())) {
					strBuilder.append(" \"").append(processSig(name, text)).append("\" ");
				} else if ("@code".equals(tag.name())) {
					strBuilder.append(" '").append(processSig(name, text)).append("' ");
				}
			}
		}
		return strBuilder.toString().replaceAll("\n", "");
	}

	@Override
	public String transformBlock(final Block block) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String transformItem(final Item item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String transformEventSummary(ApiEvent apiEvent, String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String transformEventDetail(ApiEvent apiEvent, String className) {
		final StringBuilder builder = new StringBuilder();
		if (apiEvent.returnType != null && !apiEvent.returnType.isEmpty()) {
			builder.append(apiEvent.returnType).append(" ");
		}
		builder.append(toSignature(apiEvent));
		if (apiEvent.description != null) {
			builder.append(" ").append(processText(className, apiEvent.description));
		}
		if (!apiEvent.parameters.isEmpty()) {
			builder.append("\n    Arguments are: ");
			boolean first = true;
			for (final ApiParameter parameter : apiEvent.parameters) {
				if (!first) {
					builder.append(", ");
				}
				first = false;
				builder.append(parameter);
			}
		}
		return builder.toString();
	}

	@Override
	public String transformMethodSummary(ApiMethod apiMethod, String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String transformMethodDetail(ApiMethod apiMethod, String className) {
		final StringBuilder builder = new StringBuilder();
		if (apiMethod.returnType != null && !apiMethod.returnType.isEmpty()) {
			builder.append(apiMethod.returnType).append(" ");
		}
		builder.append(toSignature(apiMethod));
		if (apiMethod.description != null) {
			builder.append(" ").append(processText(className, apiMethod.description));
		}
		for (final ApiParameter parameter : apiMethod.parameters) {
			builder.append("\n    ").append(transformParameter(parameter, className));
		}
		return builder.toString();
	}

}
