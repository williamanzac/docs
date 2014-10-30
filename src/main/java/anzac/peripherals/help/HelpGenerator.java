package anzac.peripherals.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import anzac.peripherals.help.model.ApiClass;
import anzac.peripherals.help.model.ApiEvent;
import anzac.peripherals.help.model.ApiMethod;
import anzac.peripherals.help.model.ApiParameter;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

public class HelpGenerator {
	protected static List<ApiClass> generateModel(final RootDoc rootDoc) {
		final List<ApiClass> classes = new ArrayList<ApiClass>();
		for (final ClassDoc classDoc : rootDoc.classes()) {
			for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
				if (annotationDesc.annotationType().toString().endsWith("Peripheral")) {
					final ApiClass processClass = toApiClass(classDoc, annotationDesc);
					classes.add(processClass);
				}
			}
		}
		return classes;
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
		method.description = methodDoc.commentText();
		method.returnType = javaToLUA(methodDoc.returnType());
		method.parameters.addAll(generateParameterModel(methodDoc));
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
				for (final ElementValuePair pair : annotationDesc.elementValues()) {
					if (pair.element().name().equals("value")) {
						eventType = ((Enum<?>) pair.value().value()).name();
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
		event.description = methodDoc.commentText();
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
		final String[] strings = StringUtils.split(tag.text(), "\n");
		final String name = StringUtils.trim(strings[0]);
		apiParameter.name = name;
		if (strings.length > 1) {
			apiParameter.description = tag.text();
		}
		return apiParameter;
	}

	private static String javaToLUA(final Type type) {
		final String typeName = type.simpleTypeName();
		if ("map".equalsIgnoreCase(typeName)) {
			return "table";
		} else if ("long".equalsIgnoreCase(typeName) || "int".equalsIgnoreCase(typeName)
				|| "double".equalsIgnoreCase(typeName) || "float".equalsIgnoreCase(typeName)) {
			return "number";
		} else if ("void".equalsIgnoreCase(typeName)) {
			return "";
		} else if (StringUtils.isNotBlank(type.dimension())) {
			return "array";
		}

		// boolean, string
		return typeName.toLowerCase();
	}
}
