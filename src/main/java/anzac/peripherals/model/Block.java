package anzac.peripherals.model;

import java.util.ArrayList;
import java.util.List;

import com.sun.javadoc.Tag;

public class Block {
	public String name;
	public Tag[] description;
	public ApiClass peripheral;
	public final List<Item> items = new ArrayList<>();
	public String tool;
	public int toolLevel;

}
