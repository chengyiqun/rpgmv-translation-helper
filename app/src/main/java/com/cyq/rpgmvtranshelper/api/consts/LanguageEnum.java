package com.cyq.rpgmvtranshelper.api.consts;

/**
 * @author cheng
 */

public enum LanguageEnum {
	/*
	 * 自动
	 */
	AUTO("auto"),

	/*
	 * 英语
	 */
	ENG("en"),

	/*
	 * 日语
	 */
	JAPAN("ja"),

	/*
	 * 中文
	 */
	ZH_CN("zh-CN");


	private final String name;
	LanguageEnum(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
