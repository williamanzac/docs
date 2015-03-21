package anzac.peripherals.xml;

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

public class XMLTransformer implements Transformer {

	@Override
	public String transformClass(final ApiClass apiClass) {
		final StrBuilder builder = new StrBuilder();
		builder.append("<apiClass>");
		builder.append("<type>" + apiClass.type + "</type>");
		if (!apiClass.methods.isEmpty()) {
			builder.append("<methods>");
			for (final ApiMethod methodXML : apiClass.methods) {
				builder.append(transformMethodDetail(methodXML, apiClass.name));
			}
			builder.append("</methods>");
		}
		if (!apiClass.events.isEmpty()) {
			builder.append("<events>");
			for (final ApiEvent event : apiClass.events) {
				builder.append(transformEventDetail(event, apiClass.name));
			}
			builder.append("</events>");
		}
		builder.append("</apiClass>");
		return builder.toString();
	}

	@Override
	public String transformEventSummary(final ApiEvent apiEvent, final String className) {
		// should not be used
		return null;
	}

	@Override
	public String transformEventDetail(final ApiEvent apiEvent, final String className) {
		final StrBuilder builder = new StrBuilder();
		builder.append("<apiEvent>");
		builder.append("<name>" + apiEvent.name + "</name>");
		builder.append("<description>" + processText(className, apiEvent.description) + "</description>");
		if (!apiEvent.parameters.isEmpty()) {
			builder.append("<arguments>");
			for (final ApiParameter arg : apiEvent.parameters) {
				builder.append(transformParameter(arg, className));
			}
			builder.append("</arguments>");
		}
		builder.append("</apiEvent>");
		return builder.toString();
	}

	@Override
	public String transformMethodSummary(final ApiMethod apiMethod, final String className) {
		// should not be used
		return null;
	}

	@Override
	public String transformMethodDetail(final ApiMethod apiMethod, final String className) {
		final StrBuilder builder = new StrBuilder();
		builder.append("<apiMethod>");
		builder.append("<name>" + apiMethod.name + "</name>");
		builder.append("<description>" + processText(className, apiMethod.description) + "</description>");
		if (!apiMethod.parameters.isEmpty()) {
			builder.append("<parameters>");
			for (final ApiParameter parameterXML : apiMethod.parameters) {
				builder.append(transformParameter(parameterXML, className));
			}
			builder.append("</parameters>");
		}
		builder.append("<returns>" + apiMethod.returnType + "</returns>");
		builder.append("<returnDescription>" + processText(className, apiMethod.returnDescription)
				+ "</returnDescription>");
		builder.append("</apiMethod>");
		return builder.toString();
	}

	@Override
	public String transformParameter(final ApiParameter apiParameter, final String className) {
		return "<parameter><name>" + apiParameter.name + "</name><description>"
				+ processText(className, apiParameter.description) + "</description></parameter>";
	}

	@Override
	public String transformBlock(final Block block) {
		final StrBuilder builder = new StrBuilder();
		builder.append("<block>");
		builder.append("<name>" + block.name + "</name>");
		builder.append("<description>" + processText(block.name, block.description) + "</description>");
		if (!block.items.isEmpty()) {
			builder.append("<items>");
			for (final Item itemXML : block.items) {
				builder.append(transformItem(itemXML));
			}
			builder.append("</items>");
		}
		if (block.peripheral != null) {
			builder.append("<peripheral>" + transformClass(block.peripheral) + "</peripheral>");
		}
		builder.append("</block>");
		return builder.toString();
	}

	@Override
	public String transformItem(final Item item) {
		final StrBuilder builder = new StrBuilder();
		builder.append("<item>");
		builder.append("<name>" + item.name + "</name>");
		builder.append("<description>" + processText(item.name, item.description) + "</description>");
		builder.append("</item>");
		return builder.toString();
	}

	private String processText(final String name, final Tag[] tags) {
		final StrBuilder strBuilder = new StrBuilder();
		if (tags != null) {
			for (final Tag tag : tags) {
				final String text = StringUtils.strip(tag.text());
				if ("Text".equals(tag.name())) {
					strBuilder.append(text);
				} else if ("@link".equals(tag.name())) {
					strBuilder.append(" {link:" + processSig(name, text) + "} ");
				} else if ("@code".equals(tag.name())) {
					strBuilder.append(" {code:" + processSig(name, text) + "} ");
				}
			}
		}
		strBuilder.replaceAll("\n", null);
		return strBuilder.toString();
	}

	private String processSig(final String name, final String text) {
		final StrBuilder builder = new StrBuilder();
		final Pattern methodPattern = Pattern.compile("(.*)#(.*)\\(");
		final Matcher methodMatcher = methodPattern.matcher(text);
		if (methodMatcher.find()) {
			// get Class
			final String strClass = methodMatcher.group(1);
			ApiClass classXML;
			if (StringUtils.isNotBlank(strClass) && !name.equals(strClass)) {
				classXML = ModelGenerator.apiClasses.get(strClass);
			} else {
				classXML = ModelGenerator.apiClasses.get(name);
			}
			builder.append(classXML.name);
			builder.append(".");
			// get method
			final String strMethod = methodMatcher.group(2);
			if (StringUtils.isNotBlank(strMethod)) {
				for (final ApiMethod method : classXML.methods) {
					if (method.name.equals(strMethod)) {
						builder.append(method.name);
						break;
					}
				}
			}
			return builder.toString();
		}
		final Pattern eventPattern = Pattern.compile("(PeripheralEvent)#(.*)");
		final Matcher eventMatcher = eventPattern.matcher(text);
		if (eventMatcher.find()) {
			builder.append("#");
			// get method
			builder.append(eventMatcher.group(2));
			return builder.toString();
		}
		final ApiClass classXML = ModelGenerator.apiClasses.get(text);
		if (classXML != null) {
			return classXML.name;
		}
		return text;
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
}
