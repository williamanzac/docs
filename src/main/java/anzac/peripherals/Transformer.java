package anzac.peripherals;

import anzac.peripherals.model.ApiClass;
import anzac.peripherals.model.ApiEvent;
import anzac.peripherals.model.ApiMethod;
import anzac.peripherals.model.ApiParameter;
import anzac.peripherals.model.Block;
import anzac.peripherals.model.Item;

public interface Transformer {

	public abstract String transformClass(ApiClass apiClass);

	public abstract String transformEventSummary(ApiEvent apiEvent, String className);

	public abstract String transformEventDetail(ApiEvent apiEvent, String className);

	public abstract String transformMethodSummary(ApiMethod apiMethod, String className);

	public abstract String transformMethodDetail(ApiMethod apiMethod, String className);

	public abstract String transformParameter(ApiParameter apiParameter, String className);

	public abstract String transformBlock(Block block);

	public abstract String transformItem(Item item);
}
