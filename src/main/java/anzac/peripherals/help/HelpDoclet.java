package anzac.peripherals.help;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import anzac.peripherals.help.model.ApiClass;
import anzac.peripherals.help.model.ApiMethod;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class HelpDoclet {
	private static RootDoc rootDoc;
	private static final Map<String, ApiClass> apiClasses = new HashMap<String, ApiClass>();
	private static File helpDir;

	public static boolean start(final RootDoc rootDoc) {
		HelpDoclet.rootDoc = rootDoc;
		try {
			processOptions(rootDoc.options());
		} catch (final MalformedURLException e) {
			rootDoc.printError(e.getMessage());
			return false;
		}
		final List<ApiClass> model = HelpGenerator.generateModel(rootDoc);
		for (ApiClass apiClass : model) {
			apiClasses.put(apiClass.name, apiClass);
		}
		for (final ApiClass api : model) {
			try {
				final String document = api.toHelp();
				final String classFileName = api.type + "api";
				final File file = new File(helpDir, classFileName);
				rootDoc.printNotice(file.getAbsolutePath());
				try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
					out.write(document);
				}
			} catch (final Exception e) {
				e.printStackTrace();
				rootDoc.printError(e.getMessage());
				return false;
			}
		}
		return true;
	}

	private static void processOptions(final String[][] options) throws MalformedURLException {
		for (final String[] strings : options) {
			if ("-dir".equals(strings[0])) {
				helpDir = new File(strings[1]);
			}
		}
	}

	public static int optionLength(final String option) {
		if ("-dir".equals(option)) {
			return 2;
		}
		return 0;
	}

	public static boolean validOptions(final String options[][], final DocErrorReporter reporter) {
		return true;
	}

	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	protected static void log(final String message) {
		if (rootDoc != null) {
			rootDoc.printNotice(message);
		}
	}

	public static String processText(final String name, final Tag[] tags) {
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

	private static final Pattern methodPattern = Pattern.compile("(.*)#(.*)\\(");
	private static final Pattern eventPattern = Pattern.compile("(PeripheralEvent)#(.*)");

	private static String processSig(final String name, final String text) {
		final StringBuilder builder = new StringBuilder();
		final Matcher methodMatcher = methodPattern.matcher(text);
		if (methodMatcher.find()) {
			// get Class
			final String strClass = methodMatcher.group(1);
			ApiClass apiClass;
			if (strClass.length() > 0 && !name.equals(strClass)) {
				apiClass = apiClasses.get(strClass);
				if (apiClass == null) {
					builder.append(strClass);
					rootDoc.printWarning("could not find type for class " + strClass);
				} else {
					builder.append(apiClass.type);
				}
				builder.append(".");
			} else {
				apiClass = apiClasses.get(name);
			}
			// get method
			final String strMethod = methodMatcher.group(2);
			if (!strMethod.isEmpty()) {
				if (apiClass == null) {
					builder.append(strMethod);
				} else {
					for (final ApiMethod method : apiClass.methods) {
						if (method.name.equals(strMethod)) {
							builder.append(method.toSignature());
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
		final ApiClass apiClass = apiClasses.get(text);
		if (apiClass != null) {
			return apiClass.name;
		}
		return text;
	}
}
