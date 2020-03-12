package com.cyq.rpgmvtranshelper.api;

import com.cyq.rpgmvtranshelper.api.consts.URL;
import com.cyq.rpgmvtranshelper.api.entity.Configuration;
import com.cyq.rpgmvtranshelper.utils.HttpCallUtil;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cheng
 */
public class CallGoogleTranslator implements CallTranslator {
	private ObjectMapper objectMapper = new ObjectMapper();

	// 拆分正则 和原始字符串
	private static Pattern pattern = Pattern.compile(
			"\\\\V\\[[0-9]*?\\]" + "|" + /* \V[n] 第n号变量的值*/
					"\\\\N\\[[0-9]*?\\]" + "|" + /* \N[n] 第n号角色的名字*/
					"\\\\P\\[[0-9]*?\\]" + "|" + /* \P[n] 第n号（排列顺序）队员的名字*/
					"\\\\C\\[[0-9]*?\\]" + "|" + /* \C[n] 将后边的文字显示为第n号颜色。颜色序号以系统图片[Window.png]为准。*/
					"\\\\I\\[[0-9]*?\\]" + "|" + /* \I[n] 绘制第n号图标。*/
					"\\\\G" + "|" + /* \G 货币单位*/
					"\\\\\\{" + "|" + /* \{ 将之后文字大小放大一级*/
					"\\\\\\}" + "|" + /* \} 将之后文字大小缩小一级*/
					"\\\\\\\\" + "|" + /* \\ 反斜杠*/
					"\\\\\\$" + "|" + /* \$ 打开显示所持金钱的窗口*/
					"\\\\\\." + "|" + /* \. 显示文字时等待四分之一秒。*/
					"\\\\\\|" + "|" + /* \| 显示文字时等待一秒。*/
					"\\\\!" + "|" + /* \! 等待玩家按键*/
					"\\\\>" + "|" + /* \> 一次性显示同一行剩余文字。 */
					"\\\\<" + "|" + /* \< 取消一次性显示文字的效果。 */
					"\\\\\\^" + "|" + /* \^ 文字显示完成后不等待输入。 */
					/* 标点分割符*/
					"。" + "|" +
					"？" + "|" +
					"！" + "|" +
					"；" + "|" +
					"：" + "|" +
					":" + "|" +
					"「" + "|" +
					"」" + "|" +
					"『" + "|" +
					"』" + "|" +
					"（" + "|" +
					"）" + "|" +
					"【" + "|" +
					"】" + "|" +
					"—" + "|" +
					"…" + "|" +
//					"～" + "|" +
//					"〜" + "|" +
					"《" + "|" +
					"》" + "|" +
					"﹏"

			);

	private static Cache<String,String> cache = CacheBuilder.newBuilder().build();
	private static String sourceLaunguage = "auto";
	private static String targetLaunguage = "zh-CN";
	private static int timeout = 6000;
	@Override
	public String translate(String srcText) throws IOException {
		Long beganTime = System.nanoTime();
		String respStr = cache.getIfPresent(srcText);
		if (StringUtils.isNotEmpty(respStr)) {
			Long endTime = System.nanoTime();
			System.out.println("谷歌翻译命中缓存");
			return getBenchMark(srcText, beganTime, respStr, endTime);
		}

		Deque<String> symbolStack = new LinkedList<>();
		List<String> splitStringList = new LinkedList<>();
		// 字符串拆分
		Matcher matcher = pattern.matcher(srcText);
		int lastEnd = 0;
		while (matcher.find()) {
			int matchIndex = matcher.start();
			splitStringList.add(srcText.substring(lastEnd, matchIndex));
			symbolStack.add(matcher.group());
			lastEnd = matcher.end();
		}
		if (lastEnd < srcText.length()) {
			splitStringList.add(srcText.substring(lastEnd));
		}

		System.out.println("symbolStack = " + symbolStack);
		System.out.println("splitStringList = " + splitStringList);

		String preTransText = String.join("。", splitStringList);
		if ("".equals(preTransText.replace("。", ""))) {
			System.out.println("文本无需翻译");
			cache.put(srcText,srcText);
			Long endTime = System.nanoTime();
			return getBenchMark(srcText, beganTime, srcText, endTime);
		}

		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(timeout)
				.setConnectTimeout(timeout)
				.build();
		String baseUrl = URL.GOOGLE_TRANSLATION;
		String url = baseUrl +
				"?client=gtx&dt=t" +
				"&sl=" +
				sourceLaunguage +
				"&tl=" +
				targetLaunguage +
				"&q=" +
				preTransText;
		respStr = HttpCallUtil.httpGet(url, requestConfig);
		assert respStr != null;
		List list0 = objectMapper.readValue(respStr, List.class);
		List list1 = (List) list0.get(0);

		String transedText = "";
		List<String> splitTransStringList = new ArrayList<>();
		for (Object o : list1) {
			List list2 = (List) o;
			String str = StringUtils.removeEnd((String) list2.get(0), "。");
			str = StringUtils.removeEnd(str, "？");
			splitTransStringList.add(str);
		}

		if (splitTransStringList.size() == 1) {
			transedText = splitTransStringList.get(0);
		} else if (splitTransStringList.size() > 1) {
			StringBuilder sb = new StringBuilder();
			sb.append(splitTransStringList.get(0));
			ListIterator<String> listIt = splitTransStringList.listIterator(1);
			while (listIt.hasNext()) {
				sb.append(symbolStack.pop());
				sb.append(listIt.next());
			}
			transedText = sb.toString();
		}
		if (symbolStack.size() == 1) {
			transedText = transedText + symbolStack.pop();
		}
		System.out.println("transedText = " + transedText);
		cache.put(srcText,transedText);
		Long endTime = System.nanoTime();
		return getBenchMark(srcText, beganTime, transedText, endTime);
	}

	@Override
	public String translateNoCache(String text) throws IOException {
		cache.put(text, "");
		return translate(text);
	}

	private String getBenchMark(String text, Long beganTime, String respStr, Long endTime) {
		System.out.println("原文: " + text);
		System.out.println("译文: " + respStr);
		System.out.println("耗时: " + ((endTime - beganTime) / 1000000) + "毫秒");
		System.out.println("\n");
		return respStr;
	}

	@Override
	public void addConfiguration(Configuration configuration) {
		timeout = configuration.getTimeout();
		sourceLaunguage = configuration.getSourceLaunguage();
		targetLaunguage = configuration.getTargetLaunguage();
	}

	@Override
	public void replaceTransCache(String srcText, String transText) {
		cache.put(srcText,transText);
	}

	public static void main(String[] args) throws IOException {
		CallTranslator translator = new CallGoogleTranslator();
		String srcText = "こんにちは。おはようございます。";
		System.out.println("translator.translate(srcText) = " + translator.translate(srcText));
		System.out.println("translator.translate(srcText) = " + translator.translate(srcText));
	}
}
