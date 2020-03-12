import com.cyq.rpgmvtranshelper.api.CallGoogleTranslator;
import com.cyq.rpgmvtranshelper.api.CallTranslator;
import com.cyq.rpgmvtranshelper.api.consts.LanguageEnum;
import com.cyq.rpgmvtranshelper.api.entity.Configuration;
import org.junit.Test;

import java.io.IOException;

public class GoogleTranslatApiTest {
	@Test
	public void test() throws IOException {
		CallTranslator translator = new CallGoogleTranslator();
		translator.addConfiguration(Configuration.create()
				.setTimeout(3000)
				.setSourceLaunguage(LanguageEnum.AUTO)
				.setTargetLaunguage(LanguageEnum.ZH_CN));

		for (String s : "one,one,one,one,one,one,one,one,one,two,two,two,two,two,two,two".split(",")) {
			System.out.print(translator.translate(s)+", ");
		}
	}
}
