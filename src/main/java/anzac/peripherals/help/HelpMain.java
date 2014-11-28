package anzac.peripherals.help;

import com.sun.tools.javadoc.Main;

public class HelpMain {

	public static void main(String[] args) {
		Main.execute(
				HelpDoclet.class.getSimpleName(),
				HelpDoclet.class.getCanonicalName(),
				new String[] {
						"-dir",
						"D:\\workspaces\\minecraft\\1.7.10\\anzacperipherals\\src\\main\\resources\\assets\\anzacperipherals\\lua\\anzacperipherals\\help" });
	}
}
