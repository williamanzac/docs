package anzac.peripherals.wiki;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;

import anzac.peripherals.model.Block;
import anzac.peripherals.model.ModelGenerator;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

public class WikiDoclet {
	private static RootDoc rootDoc;
	private static File helpDir;

	public static boolean start(final RootDoc rootDoc) {
		WikiDoclet.rootDoc = rootDoc;
		try {
			processOptions(rootDoc.options());
		} catch (final MalformedURLException e) {
			rootDoc.printError(e.getMessage());
			return false;
		}
		ModelGenerator.generateAPIModel(rootDoc);
		ModelGenerator.generateBlockModel(rootDoc);
		MarkdownTransformer transformer = new MarkdownTransformer();
		for (final Block block : ModelGenerator.blockClasses.values()) {
			try {
				final String document = transformer.transformBlock(block);
				final String classFileName = blockName(block) + ".md";
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

	public static void log(final String message) {
		if (rootDoc != null) {
			rootDoc.printNotice(message);
		}
	}

	private static String blockName(final Block block) {
		return block.name.replaceAll("\\s+", "");
	}
}
