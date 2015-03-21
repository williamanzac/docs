package anzac.peripherals.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import anzac.peripherals.Transformer;
import anzac.peripherals.model.ApiClass;
import anzac.peripherals.model.ApiEvent;
import anzac.peripherals.model.ApiMethod;
import anzac.peripherals.model.ApiParameter;
import anzac.peripherals.model.Block;
import anzac.peripherals.model.Item;
import anzac.peripherals.model.ModelGenerator;

import com.sun.javadoc.Tag;

public class HTMLTransformer implements Transformer {

	@Override
	public String transformClass(final ApiClass apiClass) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<section id=\"api\">");
		builder.appendln("<h2>API</h2>");
		builder.appendln("<dl>");
		builder.appendln("<dt>Peripheral Type</dt>");
		builder.appendln("<dd>" + apiClass.type + "</dd>");
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
		for (final ApiMethod methodXML : apiClass.methods) {
			builder.append(transformMethodSummary(methodXML, apiClass.name));
		}
		builder.appendln("</tbody>");
		builder.appendln("</table>");
		if (!apiClass.events.isEmpty()) {
			builder.appendln("<h3>Event Summary</h3>");
			builder.appendln("<ul>");
			for (final ApiEvent event : apiClass.events) {
				builder.append(transformEventSummary(event, apiClass.name));
			}
			builder.appendln("</ul>");
		}
		builder.appendln("</section>");
		builder.appendln("<section id=\"detail\">");
		builder.appendln("<h3>Method Detail</h3>");
		for (final ApiMethod methodXML : apiClass.methods) {
			builder.append(transformMethodDetail(methodXML, apiClass.name));
		}

		if (!apiClass.events.isEmpty()) {
			builder.appendln("<h3>Event Detail</h3>");
			for (final ApiEvent event : apiClass.events) {
				builder.append(transformEventDetail(event, apiClass.name));
			}
		}
		builder.appendln("</section>");
		return builder.toString();
	}

	@Override
	public String transformEventSummary(final ApiEvent apiEvent, final String className) {
		return "<li><a href=\"#" + apiEvent.name + "\">" + apiEvent.name + "</a></li>";
	}

	@Override
	public String transformEventDetail(final ApiEvent apiEvent, final String className) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<a name=\"" + apiEvent.name + "\"></a>");
		builder.appendln("<h4>" + apiEvent.name + "</h4>");
		builder.appendln(processText(className, apiEvent.description));
		if (!apiEvent.parameters.isEmpty()) {
			builder.appendln("<dl>");
			builder.appendln("<dt>Arguments</dt>");
			for (final ApiParameter arg : apiEvent.parameters) {
				builder.appendln(transformParameter(arg, className));
			}
			builder.appendln("</dl>");
		}
		return builder.toString();
	}

	@Override
	public String transformMethodSummary(final ApiMethod apiMethod, final String className) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<tr>");
		builder.appendln("<td>" + apiMethod.returnType + "</td>");
		builder.appendln("<td><a href=\"#" + toSignature(apiMethod) + "\"><code>" + toSignature(apiMethod)
				+ "</code></a> " + processText(className, apiMethod.firstLine) + "</td>");
		builder.appendln("</tr>");
		return builder.toString();
	}

	@Override
	public String transformMethodDetail(final ApiMethod apiMethod, final String className) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<section id=\"" + toSignature(apiMethod) + ">");
		builder.appendln("<a name=\"" + toSignature(apiMethod) + "\"></a>");
		builder.appendln("<code>" + toSignatureWithReturn(apiMethod) + "</code>");
		builder.appendln(processText(className, apiMethod.description));
		final boolean notVoid = StringUtils.isNotBlank(apiMethod.returnType);
		final boolean hasParams = apiMethod.parameters != null && !apiMethod.parameters.isEmpty();
		if (notVoid || hasParams) {
			builder.appendln("<dl>");
			if (hasParams) {
				builder.appendln("<dt>Parameters</dt>");
				for (final ApiParameter parameterXML : apiMethod.parameters) {
					builder.appendln(transformParameter(parameterXML, className));
				}
			}
			if (notVoid) {
				builder.appendln("<dt>Returns</dt>");
				builder.appendln("<dd>" + processText(className, apiMethod.returnDescription) + "</dd>");
			}
			builder.appendln("</dl>");
		}
		builder.appendln("</section>");
		return builder.toString();
	}

	@Override
	public String transformParameter(final ApiParameter apiParameter, final String className) {
		return "<dd><code>" + apiParameter.name + "</code> " + processText(className, apiParameter.description)
				+ "</dd>";
	}

	@Override
	public String transformBlock(final Block block) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<section id=\"description\">");
		if (block.peripheral != null) {
			builder.appendln(processText(block.peripheral.name, block.peripheral.description));
		}
		builder.appendln("</section>");
		if (!block.items.isEmpty()) {
			builder.appendln("<section id=\"recipe\">");
			if (block.items.size() == 1) {
				builder.appendln("<h3>Recipe</h3>");
				builder.append(transformItemRecipe(block.items.get(0)));
			} else {
				builder.appendln("<h3>Recipes</h3>");
				for (final Item itemXML : block.items) {
					builder.append(transformItem(itemXML));
				}
			}
			builder.appendln("</section>");
		}
		if (block.peripheral != null) {
			builder.append(transformClass(block.peripheral));
		}
		return builder.toString();
	}

	@Override
	public String transformItem(final Item item) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<section id=\"description\">");
		builder.appendln(processText(item.name, item.description));
		builder.appendln("</section>");
		builder.appendln("<section id=\"recipe\">");
		builder.appendln("<h3>Recipe</h3>");
		builder.appendln(transformItemRecipe(item));
		builder.appendln("</section>");
		return builder.toString();
	}

	public String transformItemRecipe(final Item item) {
		return "<img alt=\"Recipe for " + camelCaseItemName(item.name)
				+ "\" src=\"http://files.anzacgaming.co.uk/images/recipe_" + lowerUnderscoreItemName(item.name)
				+ ".png\" />";
	}

	private String processText(final String name, final Tag[] tags) {
		final StrBuilder strBuilder = new StrBuilder();
		if (tags != null) {
			for (final Tag tag : tags) {
				final String text = StringUtils.strip(tag.text());
				if ("Text".equals(tag.name())) {
					strBuilder.append(text);
				} else if ("@link".equals(tag.name())) {
					strBuilder.append(" <a href=\"").append(processSig(name, text, true)).append("\"><code>")
							.append(processSig(name, text, false)).append("</code></a> ");
				} else if ("@code".equals(tag.name())) {
					strBuilder.append(" <code>").append(processSig(name, text, false)).append("</code> ");
				}
			}
		}
		strBuilder.replaceAll("\n", null);
		return strBuilder.toString();
	}

	private String toSignature(final ApiMethod apiMethod) {
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

	private String toSignatureWithReturn(final ApiMethod apiMethod) {
		final StringBuilder builder = new StringBuilder();
		if (StringUtils.isNotBlank(apiMethod.returnType)) {
			builder.append(apiMethod.returnType).append(" ");
		}
		builder.append(toSignature(apiMethod));
		return builder.toString();
	}

	private String processSig(final String name, final String text, final boolean link) {
		final StrBuilder builder = new StrBuilder();
		final Pattern methodPattern = Pattern.compile("(.*)#(.*)\\(");
		final Matcher methodMatcher = methodPattern.matcher(text);
		if (methodMatcher.find()) {
			// get Class
			final String strClass = methodMatcher.group(1);
			ApiClass classXML;
			if (StringUtils.isNotBlank(strClass) && !name.equals(strClass)) {
				classXML = ModelGenerator.apiClasses.get(strClass);
				if (!link) {
					builder.append(className(classXML));
					builder.append(".");
				} else {
					builder.append(classFullFileName(classXML));// baseUrl +
				}
			} else {
				classXML = ModelGenerator.apiClasses.get(name);
			}
			if (link) {
				builder.append("#");
			}
			// get method
			final String strMethod = methodMatcher.group(2);
			if (StringUtils.isNotBlank(strMethod)) {
				for (final ApiMethod method : classXML.methods) {
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
		final ApiClass classXML = ModelGenerator.apiClasses.get(text);
		if (classXML != null) {
			if (link) {
				return classFullFileName(classXML);// baseUrl +
			}
			return className(classXML);
		}
		return text;
	}

	protected static String className(final ApiClass apiClass) {
		final String typeName = apiClass.name;
		final int index = typeName.indexOf("TileEntity");
		final String fileName = typeName.substring(0, index);
		return fileName;
	}

	private static String classFullFileName(final ApiClass classXML) {
		final String lowerDash = lowerDash(className(classXML));
		final String pkg = classXML.fullName.replace("anzac.peripherals", "anzac-peripherals")
				.replace("peripheral.", "blocks.").replace(classXML.name, lowerDash).replaceAll("\\.", "/");
		return pkg;
	}

	protected static String camelCase(final String input) {
		if (input == null) {
			return null;
		}
		final StrBuilder builder = new StrBuilder();
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				builder.append(" ");
			}
			builder.append(c);
		}
		return builder.toString();
	}

	protected static String lowerUnderscore(String input) {
		if (input == null) {
			return null;
		}
		input = StringUtils.deleteWhitespace(input);
		final StrBuilder builder = new StrBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				builder.append("_");
			}
			c = Character.toLowerCase(c);
			builder.append(c);
		}
		return builder.toString();
	}

	protected static String camelCaseItemName(final String input) {
		if (input == null) {
			return null;
		}
		boolean lastSpace = true;
		final StrBuilder builder = new StrBuilder();
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (lastSpace) {
				builder.append(Character.toUpperCase(c));
				lastSpace = false;
			} else if (c == '_' || c == ' ') {
				builder.append(" ");
				lastSpace = true;
			} else {
				builder.append(Character.toLowerCase(c));
			}
		}
		return builder.toString();
	}

	protected static String lowerUnderscoreItemName(final String input) {
		if (input == null) {
			return null;
		}
		return input.replaceAll("\\s", "_").toLowerCase();
	}

	protected static String lowerDash(final String input) {
		if (input == null) {
			return null;
		}
		final StrBuilder builder = new StrBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				builder.append("-");
			}
			c = Character.toLowerCase(c);
			builder.append(c);
		}
		return builder.toString();
	}
}
