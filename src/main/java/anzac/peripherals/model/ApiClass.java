package anzac.peripherals.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.javadoc.Tag;

public class ApiClass {
	public String name;
	public String type;
	public Tag[] description;
	public final Set<ApiMethod> methods = new TreeSet<ApiMethod>();
	public final List<ApiEvent> events = new ArrayList<ApiEvent>();
	public String fullName;
}
