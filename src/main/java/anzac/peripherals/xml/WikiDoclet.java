package anzac.peripherals.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;

import org.apache.commons.lang.StringUtils;

import anzac.peripherals.model.Block;
import anzac.peripherals.model.Item;
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
		ModelGenerator.generateItemModel(rootDoc);
		XMLTransformer transformer = new XMLTransformer();
		for (final Block block : ModelGenerator.blockClasses.values()) {
			try {
				final String document = transformer.transformBlock(block);
				final String classFileName = blockName(block) + ".xml";
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
		for (final Item item : ModelGenerator.itemClasses.values()) {
			try {
				final String document = transformer.transformItem(item);
				final String classFileName = itemName(item) + ".xml";
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

	public static void log(final String message) {
		if (rootDoc != null) {
			rootDoc.printNotice(message);
		}
	}

	private static String blockName(final Block blockXML) {
		return StringUtils.deleteWhitespace(blockXML.name);
	}

	private static String itemName(final Item itemXML) {
		return StringUtils.deleteWhitespace(XMLTransformer.camelCaseItemName(itemXML.name));
	}
}
