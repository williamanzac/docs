package anzac.peripherals.help;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;

import anzac.peripherals.Transformer;
import anzac.peripherals.model.ApiClass;
import anzac.peripherals.model.ModelGenerator;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

public class HelpDoclet {
	private static RootDoc rootDoc;
	private static File helpDir;

	public static boolean start(final RootDoc rootDoc) {
		HelpDoclet.rootDoc = rootDoc;
		try {
			processOptions(rootDoc.options());
		} catch (final MalformedURLException e) {
			rootDoc.printError(e.getMessage());
			return false;
		}
		ModelGenerator.generateAPIModel(rootDoc);
		final Transformer transformer = new HelpTransformer();
		for (final ApiClass api : ModelGenerator.apiClasses.values()) {
			try {
				final String document = transformer.transformClass(api);
				final String classFileName = api.type + "s";
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
			if ("-d".equals(strings[0])) {
				helpDir = new File(strings[1]);
			}
		}
	}

	public static int optionLength(final String option) {
		if ("-d".equals(option)) {
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

	protected static void logWarning(final String message) {
		if (rootDoc != null) {
			rootDoc.printWarning(message);
		}
	}

	protected static void logError(final String message) {
		if (rootDoc != null) {
			rootDoc.printError(message);
		}
	}
}
