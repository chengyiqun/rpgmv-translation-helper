package com.cyq.rpgmvtranshelper.api.entity;

import com.cyq.rpgmvtranshelper.api.consts.LanguageEnum;

public class Configuration {
	private int timeout = 6000;
	private String sourceLaunguage = "auto";
	private String targetLaunguage = "zh-CN";

	private Configuration(){}

	public static Configuration create() {
		return new Configuration();
	}

	public Configuration setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public Configuration setSourceLaunguage(LanguageEnum languageEnum) {
		this.sourceLaunguage = languageEnum.toString();
		return this;
	}

	public Configuration setTargetLaunguage(LanguageEnum languageEnum) {
		this.targetLaunguage = languageEnum.toString();
		return this;
	}


	public int getTimeout() {
		return timeout;
	}

	public String getSourceLaunguage() {
		return sourceLaunguage;
	}

	public String getTargetLaunguage() {
		return targetLaunguage;
	}
}
