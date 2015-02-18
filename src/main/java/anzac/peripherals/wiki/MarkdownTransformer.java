package anzac.peripherals.wiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import anzac.peripherals.Transformer;
import anzac.peripherals.model.ApiClass;
import anzac.peripherals.model.ApiEvent;
import anzac.peripherals.model.ApiMethod;
import anzac.peripherals.model.ApiParameter;
import anzac.peripherals.model.Block;
import anzac.peripherals.model.Item;
import anzac.peripherals.model.ModelGenerator;

import com.sun.javadoc.Tag;

public class MarkdownTransformer implements Transformer {

	@Override
	public String transformClass(final ApiClass apiClass) {
		final StringBuilder builder = new StringBuilder();
		builder.append("\n");
		builder.append("##API##\n");
		builder.append("<dl>\n");
		builder.append("<dt>Peripheral Type</dt>\n");
		builder.append("<dd>" + apiClass.type + "</dd>\n");
		builder.append("</dl>\n");
		builder.append("\n###Method Summary###\n");
		builder.append("<table>\n");
		builder.append("<tr>\n");
		builder.append("<th>Method</th>\n<th>Description</th>\n");
		builder.append("</tr>\n");
		for (final ApiMethod apiMethod : apiClass.methods) {
			builder.append("<tr>\n");
			builder.append(transformMethodSummary(apiMethod, apiClass.name));
			builder.append("</tr>\n");
		}
		builder.append("</table>\n");
		if (!apiClass.events.isEmpty()) {
			builder.append("\n###Event Summary###\n");
			for (final ApiEvent event : apiClass.events) {
				builder.append("* ").append(transformEventSummary(event, apiClass.name)).append("\n");
			}
		}
		builder.append("\n###Method Details###\n");
		for (final ApiMethod apiMethod : apiClass.methods) {
			builder.append(transformMethodDetail(apiMethod, apiClass.name)).append("\n");
		}

		if (!apiClass.events.isEmpty()) {
			builder.append("\n###Event Details###\n\n");
			for (final ApiEvent event : apiClass.events) {
				builder.append(transformEventDetail(event, apiClass.name)).append("\n");
			}
		}
		return builder.toString();
	}

	@Override
	public String transformEventSummary(final ApiEvent apiEvent, final String className) {
		return "[" + apiEvent.name + "](#" + apiEvent.name + ")";
	}

	@Override
	public String transformEventDetail(final ApiEvent apiEvent, final String className) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<a name=\"" + apiEvent.name + "\"></a>\n\n");
		builder.append("\n<code>" + toSignatureWithReturn(apiEvent) + "</code>\n");
		builder.append(processText(className, apiEvent.description)).append("\n");
		if (!apiEvent.parameters.isEmpty()) {
			builder.append("<dl>\n");
			builder.append("<dt>Arguments</dt>\n");
			for (final ApiParameter arg : apiEvent.parameters) {
				builder.append(transformParameter(arg, className)).append("\n");
			}
			builder.append("</dl>\n");
		}
		return builder.toString();
	}

	@Override
	public String transformMethodSummary(final ApiMethod apiMethod, final String className) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<tr>\n");
		builder.append("<td><a href=\"#" + toSignature(apiMethod) + "\"><code>" + toSignature(apiMethod)
				+ "</code></a></td>\n");
		builder.append("<td>" + processText(className, apiMethod.firstLine) + "</td>\n");
		builder.append("</tr>\n");
		return builder.toString();
	}

	@Override
	public String transformMethodDetail(final ApiMethod apiMethod, final String className) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<a name=\"" + toSignature(apiMethod) + "\"></a>\n");
		builder.append("<code>" + toSignatureWithReturn(apiMethod) + "</code>\n");
		builder.append(processText(className, apiMethod.description)).append("\n");
		final boolean notVoid = apiMethod != null && !apiMethod.returnType.isEmpty();
		final boolean hasParams = apiMethod.parameters != null && !apiMethod.parameters.isEmpty();
		if (notVoid || hasParams) {
			builder.append("<dl>\n");
			if (hasParams) {
				builder.append("<dt>Parameters</dt>\n");
				for (final ApiParameter apiParameter : apiMethod.parameters) {
					builder.append(transformParameter(apiParameter, className)).append("\n");
				}
			}
			if (notVoid) {
				builder.append("<dt>Returns</dt>\n");
				builder.append("<dd>" + processText(className, apiMethod.returnDescription) + "</dd>\n");
			}
			builder.append("</dl>\n");
		}
		return builder.toString();
	}

	@Override
	public String transformParameter(final ApiParameter apiParameter, final String className) {
		return "<dd><code>" + apiParameter.name + "</code> " + processText(className, apiParameter.description)
				+ "</dd>";
	}

	@Override
	public String transformBlock(final Block block) {
		final StringBuilder builder = new StringBuilder();
		if (block.peripheral != null) {
			builder.append(processText(block.peripheral.name, block.peripheral.description)).append("\n");
		}
		if (!block.items.isEmpty()) {
			builder.append("\n");
			if (block.items.size() == 1) {
				builder.append("###Recipe###\n");
				builder.append(transformItem(block.items.get(0))).append("\n");
			} else {
				builder.append("###Recipes###\n");
				for (final Item item : block.items) {
					builder.append(transformItem(item)).append("\n");
				}
			}
		}
		if (block.peripheral != null) {
			builder.append("\n");
			builder.append(transformClass(block.peripheral));
		}
		return builder.toString();
	}

	@Override
	public String transformItem(final Item item) {
		final StringBuilder builder = new StringBuilder();
		builder.append(processText(item.name, item.description)).append("\n");
		builder.append("\n");
		builder.append("###Recipe###\n");
		// builder.append(transformItem(block.items.get(0))).append("\n");
		return builder.toString();
	}

	private String processText(final String name, final Tag[] tags) {
		final StringBuilder strBuilder = new StringBuilder();
		if (tags != null) {
			for (final Tag tag : tags) {
				final String text = tag.text().trim();
				if ("Text".equals(tag.name())) {
					strBuilder.append(text);
				} else if ("@link".equals(tag.name())) {
					strBuilder.append(" [`").append(processSig(name, text, false)).append("`](")
							.append(processSig(name, text, true)).append(") ");
				} else if ("@code".equals(tag.name())) {
					strBuilder.append(" `").append(processSig(name, text, false)).append("` ");
				}
			}
		}
		return strBuilder.toString().replaceAll("\n", "");
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

	public String toSignatureWithReturn(final ApiMethod apiMethod) {
		final StringBuilder builder = new StringBuilder();
		if (StringUtils.isNotBlank(apiMethod.returnType)) {
			builder.append(apiMethod.returnType).append(" ");
		}
		builder.append(toSignature(apiMethod));
		return builder.toString();
	}

	private String processSig(final String name, final String text, final boolean link) {
		final StringBuilder builder = new StringBuilder();
		final Pattern methodPattern = Pattern.compile("(.*)#(.*)\\(");
		final Matcher methodMatcher = methodPattern.matcher(text);
		if (methodMatcher.find()) {
			// get Class
			final String strClass = methodMatcher.group(1);
			ApiClass apiClass;
			if (strClass.length() > 0 && !name.equals(strClass)) {
				apiClass = ModelGenerator.apiClasses.get(strClass);
				if (!link) {
					builder.append(className(apiClass));
					builder.append(".");
				} else {
					builder.append(classFullFileName(apiClass));
				}
			} else {
				apiClass = ModelGenerator.apiClasses.get(name);
			}
			if (link) {
				builder.append("#");
			}
			// get method
			final String strMethod = methodMatcher.group(2);
			if (!strMethod.isEmpty()) {
				for (final ApiMethod method : apiClass.methods) {
					if (method.name.equals(strMethod)) {
						builder.append(toSignature(method));
						break;
					}
				}
			}
			return builder.toString();
		}
		final Pattern eventPattern = Pattern.compile("(PeripheralEvent)#(.*)");
		final Matcher eventMatcher = eventPattern.matcher(text);
		if (eventMatcher.find()) {
			if (link) {
				builder.append("#");
			}
			// get method
			builder.append(eventMatcher.group(2));
			return builder.toString();
		}
		final ApiClass apiClass = ModelGenerator.apiClasses.get(text);
		if (apiClass != null) {
			if (link) {
				return classFullFileName(apiClass);
			}
			return className(apiClass);
		}
		return text;
	}

	protected static String className(final ApiClass apiClass) {
		final String typeName = apiClass.name;
		int index = typeName.indexOf("Peripheral");
		if (index < 0) {
			index = typeName.indexOf("TileEntity");
		}
		if (index > 0) {
			final String fileName = typeName.substring(0, index);
			return fileName;
		}
		WikiDoclet.log("could not get class name for: " + typeName);
		return typeName;
	}

	private static String classFullFileName(final ApiClass apiClass) {
		// final String lowerDash = lowerDash(className(apiClass));
		// final String pkg = apiClass.fullName.replace("anzac.peripherals", "anzac-peripherals")
		// .replace("peripheral.", "blocks.").replace(apiClass.name, lowerDash).replaceAll("\\.", "/");
		// return pkg;
		return className(apiClass);
	}

	// private static String lowerDash(final String input) {
	// if (input == null) {
	// return null;
	// }
	// final StringBuilder builder = new StringBuilder();
	// for (int i = 0; i < input.length(); i++) {
	// char c = input.charAt(i);
	// if (i > 0 && Character.isUpperCase(c)) {
	// builder.append("-");
	// }
	// c = Character.toLowerCase(c);
	// builder.append(c);
	// }
	// return builder.toString();
	// }
}
