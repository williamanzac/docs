package anzac.peripherals.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import anzac.peripherals.wiki.WikiDoclet;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

public class ModelGenerator {

	public static final Map<String, ApiClass> apiClasses = new HashMap<>();
	public static final Map<String, Block> blockClasses = new HashMap<>();
	public static final Map<String, Item> itemClasses = new HashMap<>();

	public static void generateAPIModel(final RootDoc rootDoc) {
		for (final ClassDoc classDoc : rootDoc.classes()) {
			for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
				if (annotationDesc.annotationType().toString().endsWith("Peripheral")) {
					final ApiClass processClass = toApiClass(classDoc, annotationDesc);
					apiClasses.put(processClass.name, processClass);
				}
			}
		}
	}

	public static void generateBlockModel(final RootDoc rootDoc) {
		for (final ClassDoc classDoc : rootDoc.classes()) {
			for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
				if (annotationDesc.annotationType().toString().endsWith("BlockInfo")) {
					final Block processClass = toBlock(classDoc, annotationDesc);
					blockClasses.put(processClass.name, processClass);
				}
			}
		}
	}

	private static Block toBlock(final ClassDoc classDoc, final AnnotationDesc annotationDesc) {
		final Block block = new Block();
		block.description = classDoc.inlineTags();
		block.name = classDoc.simpleTypeName();
		block.peripheral = apiClasses.get(block.name + "TileEntity");
		return block;
	}

	private static ApiClass toApiClass(final ClassDoc classDoc, final AnnotationDesc annotationDesc) {
		final ApiClass apiClass = new ApiClass();
		for (final ElementValuePair pair : annotationDesc.elementValues()) {
			if (pair.element().name().equals("type")) {
				apiClass.type = pair.value().value().toString();
			}
		}
		apiClass.methods.addAll(generateMethodModel(classDoc));
		apiClass.events.addAll(generateEventModel(classDoc));
		apiClass.description = classDoc.inlineTags();
		apiClass.name = classDoc.simpleTypeName();
		apiClass.fullName = classDoc.qualifiedTypeName();
		return apiClass;
	}

	private static Set<ApiMethod> generateMethodModel(final ClassDoc classDoc) {
		final Set<ApiMethod> methods = new HashSet<ApiMethod>();
		for (final MethodDoc methodDoc : classDoc.methods()) {
			final ApiMethod apiMethod = toApiMethod(methodDoc);
			if (apiMethod != null) {
				methods.add(apiMethod);
			}
		}
		final ClassDoc superclass = classDoc.superclass();
		if (superclass != null) {
			methods.addAll(generateMethodModel(superclass));
		}
		return methods;
	}

	private static ApiMethod toApiMethod(final MethodDoc methodDoc) {
		boolean peripheralMethod = false;
		for (final AnnotationDesc annotationDesc : methodDoc.annotations()) {
			if (annotationDesc.annotationType().toString().contains("Peripheral")) {
				peripheralMethod = true;
				break;
			}
		}
		if (!peripheralMethod) {
			return null;
		}
		final ApiMethod method = new ApiMethod();
		method.name = methodDoc.name();
		method.description = methodDoc.inlineTags();
		method.returnType = javaToLUA(methodDoc.returnType());
		method.parameters.addAll(generateParameterModel(methodDoc));
		method.firstLine = methodDoc.firstSentenceTags();
		for (final Tag tag : methodDoc.tags()) {
			final String tagName = tag.name();
			if (tagName.equals("@return")) {
				method.returnDescription = tag.inlineTags();
				break;
			}
		}
		return method;
	}

	private static Set<ApiEvent> generateEventModel(final ClassDoc classDoc) {
		final Set<ApiEvent> methods = new HashSet<ApiEvent>();
		for (final MethodDoc methodDoc : classDoc.methods()) {
			final ApiEvent apiMethod = toApiEvent(methodDoc);
			if (apiMethod != null) {
				methods.add(apiMethod);
			}
		}
		final ClassDoc superclass = classDoc.superclass();
		if (superclass != null) {
			methods.addAll(generateEventModel(superclass));
		}
		return methods;
	}

	private static ApiEvent toApiEvent(final MethodDoc methodDoc) {
		String eventType = null;
		for (final AnnotationDesc annotationDesc : methodDoc.annotations()) {
			if (annotationDesc.annotationType().toString().contains("Event")) {
				WikiDoclet.log("event method: " + methodDoc.qualifiedName());
				for (final ElementValuePair pair : annotationDesc.elementValues()) {
					WikiDoclet.log("pair: " + pair.element().name() + ", " + pair.value().toString());
					if (pair.element().name().equals("value")) {
						final FieldDoc fieldDoc = (FieldDoc) pair.value().value();
						eventType = fieldDoc.name();
						break;
					}
				}
			}
		}

		if (eventType == null) {
			return null;
		}
		final ApiEvent event = new ApiEvent();
		event.name = eventType;
		event.description = methodDoc.inlineTags();
		event.parameters.addAll(generateParameterModel(methodDoc));
		return event;
	}

	private static Collection<ApiParameter> generateParameterModel(MethodDoc methodDoc) {
		final Map<String, ApiParameter> parameters = new HashMap<String, ApiParameter>();
		for (final Tag tag : methodDoc.tags()) {
			final String tagName = tag.name();
			if (tagName.equals("@param")) {
				final ApiParameter apiParameter = toApiParameter(tag);
				parameters.put(apiParameter.name, apiParameter);
			}
		}
		for (final Parameter parameter : methodDoc.parameters()) {
			final String name = parameter.name();
			ApiParameter apiParameter = parameters.get(name);
			if (apiParameter == null) {
				apiParameter = new ApiParameter();
				apiParameter.name = parameter.name();
				parameters.put(apiParameter.name, apiParameter);
			}
			apiParameter.type = javaToLUA(parameter.type());
		}
		return parameters.values();
	}

	private static ApiParameter toApiParameter(Tag tag) {
		final ApiParameter apiParameter = new ApiParameter();
		final String[] strings = tag.text().split("\n");
		final String name = strings[0].trim();
		apiParameter.name = name;
		if (strings.length > 1) {
			apiParameter.description = tag.inlineTags();
		}
		return apiParameter;
	}

	protected static String javaToLUA(final Type type) {
		final String typeName = type.simpleTypeName();
		if ("map".equalsIgnoreCase(typeName)) {
			return "table";
		} else if ("long".equalsIgnoreCase(typeName) || "int".equalsIgnoreCase(typeName)
				|| "double".equalsIgnoreCase(typeName) || "float".equalsIgnoreCase(typeName)) {
			return "number";
		} else if ("void".equalsIgnoreCase(typeName)) {
			return "";
		} else if (type.dimension() != null && !type.dimension().isEmpty()) {
			return "array";
		}

		// boolean, string
		return typeName.toLowerCase();
	}

	protected static String javaToLUA(final String typeName) {
		if ("map".equalsIgnoreCase(typeName)) {
			return "table";
		} else if ("long".equalsIgnoreCase(typeName) || "int".equalsIgnoreCase(typeName)
				|| "double".equalsIgnoreCase(typeName) || "float".equalsIgnoreCase(typeName)) {
			return "number";
		}

		// boolean, string
		return typeName.toLowerCase();
	}
}
