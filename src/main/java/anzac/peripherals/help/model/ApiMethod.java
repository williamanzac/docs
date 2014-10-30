package anzac.peripherals.help.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ApiMethod {
	public String name;
	public String description;
	public String returnType;
	public List<ApiParameter> parameters = new ArrayList<ApiParameter>();

	public String toHelp() {
		final StringBuilder builder = new StringBuilder();
		builder.append(returnType).append(" ").append(name).append("( ");
		StringUtils.join(parameters, ", ");
		builder.append(" )");
		if (StringUtils.isNoneBlank(description)) {
			builder.append(" ").append(description);
		}
		return builder.toString();
	}
}
