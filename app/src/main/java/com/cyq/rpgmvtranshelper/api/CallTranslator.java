package com.cyq.rpgmvtranshelper.api;

import com.cyq.rpgmvtranshelper.api.entity.Configuration;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

public interface CallTranslator {
	String translate(String text) throws IOException;

	String translateNoCache(String text) throws IOException;

	void addConfiguration(Configuration configuration);

	void replaceTransCache(String srcText, String transText);
}
