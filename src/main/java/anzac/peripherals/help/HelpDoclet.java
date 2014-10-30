package anzac.peripherals.help;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import anzac.peripherals.help.model.ApiClass;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

public class HelpDoclet {
	public static boolean start(final RootDoc rootDoc) {
		try {
			processOptions(rootDoc.options());
		} catch (final MalformedURLException e) {
			rootDoc.printError(e.getMessage());
			return false;
		}
		List<ApiClass> model = HelpGenerator.generateModel(rootDoc);
		for (final ApiClass api : model) {
			try {
				final String document = api.toHelp();
				final String classFileName = api.type;
				if (classFileName != null) {
					final File file = new File(classFileName);
					FileUtils.writeStringToFile(file, document);
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
	}

	public static int optionLength(final String option) {
		return 0;
	}

	public static boolean validOptions(final String options[][], final DocErrorReporter reporter) {
		return true;
	}

	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}
}
