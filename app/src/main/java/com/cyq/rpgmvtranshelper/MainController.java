package com.cyq.rpgmvtranshelper;

import com.cyq.rpgmvtranshelper.api.CallGoogleTranslator;
import com.cyq.rpgmvtranshelper.api.CallTranslator;
import com.cyq.rpgmvtranshelper.api.consts.LanguageEnum;
import com.cyq.rpgmvtranshelper.api.entity.Configuration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;

/**
 * @author cheng
 */
public class MainController implements Initializable {
	private static Map<String, List<List<String>>> srcTextListMap = new ConcurrentHashMap<>();
	private static Map<String, List<List<String>>> transTextListMap = new ConcurrentHashMap<>();
	private static Map<String, Map<String,Object>> transMapFileMap = new ConcurrentHashMap<>();
	private static final String BACKUP_FOLDER_NAME = "backup";
	private CallTranslator translator;

	private String workFolderPath = "";

	@FXML
	private AnchorPane rootLayout;
	@FXML
	private VBox mapJsonVBox;
	@FXML
	private VBox translateItemVBox;
	@FXML
	private ScrollPane transScrollPane;
	@FXML
	private TextField pathTextArea;

	/**
	 * 初始化方法
	 *
	 * @param location
	 * @param resources
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		translator = new CallGoogleTranslator();
		translator.addConfiguration(Configuration.create()
				.setTimeout(3000)
				.setSourceLaunguage(LanguageEnum.AUTO)
				.setTargetLaunguage(LanguageEnum.ZH_CN));
	}

	/**
	 * 打开文件夹
	 *
	 * @param event
	 */
	@FXML
	public void openFolderAction(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("打开游戏的data文件夹");
		if (StringUtils.isNotEmpty(workFolderPath)) {
			chooser.setInitialDirectory(new File(workFolderPath));
		}
		Stage stage = (Stage) rootLayout.getScene().getWindow();
		File fileFolder = chooser.showDialog(stage);
		if (fileFolder == null) {
			return;
		}
		workFolderPath = fileFolder.getAbsolutePath();
		System.out.println(workFolderPath);
		// 避免点到备份目录
		if (workFolderPath.endsWith(BACKUP_FOLDER_NAME)) {
			workFolderPath = StringUtils.removeEnd(workFolderPath, '\\' + BACKUP_FOLDER_NAME);
			fileFolder = fileFolder.getParentFile();
		}
		Collection<File> fileList = FileUtils.listFiles(fileFolder, new String[]{"json"}, false);
		if (!checkMapXxxDotJson(fileList)) {
			Alert alert = new Alert(WARNING);
			alert.setContentText("请选择合法的data目录");
			alert.setHeaderText("温馨提示");
			alert.showAndWait();
			return;
		}
		pathTextArea.setText(workFolderPath);
		System.out.println(fileList);
		for (File file : fileList) {
			Button button = new Button();
			button.setText(file.getName());
			button.setOnAction(event1 -> {
				System.out.println(button.getText());
				File mapFile = new File(workFolderPath + "\\" + button.getText());
				// System.out.println(newFile.exists());
				// TODO
				try {
					showMapXxxTranslator(mapFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			mapJsonVBox.getChildren().add(button);
		}
	}

	/**
	 * 检查目录是否是合法的data目录
	 *
	 * @param fileList
	 * @return
	 */
	private boolean checkMapXxxDotJson(Collection<File> fileList) {
		Iterator<File> it = fileList.iterator();
		while (it.hasNext()) {
			File file = it.next();
			String fileName = file.getName();
			if (fileName.startsWith("Map") && fileName.endsWith(".json")) {
				if (StringUtils.isNumeric(fileName.substring(3, fileName.indexOf(".json")))) {
					if (file.length() > 0) {
						// 如果含有code=401的内容(代变文本
						try {
							if (has401codeTextInfo(file)) {
								continue;
							}
						} catch (IOException e) {
							Alert alert = new Alert(ERROR);
							alert.setContentText("json文件非法");
							alert.setHeaderText("错误");
							alert.showAndWait();
							return false;
						}
					}
				}
			}
			it.remove();
		}
		return fileList.size() > 0;
	}

	private boolean has401codeTextInfo(File file) throws IOException {
		if (file == null) {
			return false;
		}
		if (file.isDirectory()) {
			return false;
		}
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {
		});
		return has401codeTextInfo(map);
	}

	private boolean has401codeTextInfo(Map<String, Object> map) {
		List<Map<String, Object>> events = (List<Map<String, Object>>) map.get("events");
		if (events != null && events.size() > 0) {
			for (Map<String, Object> event : events) {
				if (event != null) {
					List<Map<String, Object>> pages = (List<Map<String, Object>>) event.get("pages");
					if (pages != null && pages.size() > 0) {
						for (Map<String, Object> page : pages) {
							List<Map<String, Object>> list = (List<Map<String, Object>>) page.get("list");
							if (list != null && list.size() > 0) {
								for (Map<String, Object> stringObjectMap : list) {
									if (stringObjectMap != null) {
										if (401 == (int) stringObjectMap.get("code")) {
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private void showMapXxxTranslator(File mapFile) throws IOException {
		translateItemVBox.getChildren().clear();

		// TODO 可以记住窗口位置
		transScrollPane.setHvalue(0);
		transScrollPane.setVvalue(0);

		String mapFileName = mapFile.getName();
		// 检查是否有备份文件
		File backMapFile = new File(workFolderPath + '\\' + BACKUP_FOLDER_NAME + '\\' + mapFileName);
		// 如果无, 则备份文件,
		if (!backMapFile.exists() || backMapFile.length() == 0) {
			FileUtils.copyFile(mapFile,backMapFile);
		}
		List<List<String>> srcTextList = srcTextListMap.get(mapFileName);
		List<List<String>> transTextList = transTextListMap.get(mapFileName);
		//从备份文件里读取作为原文, 从现有文件里读取, 作为待译文, 增加按钮操作翻译
		if (srcTextList == null) {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> map = objectMapper.readValue(backMapFile, new TypeReference<Map<String, Object>>() {
			});
			srcTextList = extractTextFromMap(map);
			srcTextListMap.put(mapFileName, srcTextList);
		}
		if (transTextList == null) {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> map = objectMapper.readValue(mapFile, new TypeReference<Map<String, Object>>() {
			});
			transMapFileMap.put(mapFileName, map);
			transTextList = extractTextFromMap(map);
			transTextListMap.put(mapFileName, transTextList);
		}
		if (srcTextList.size() != transTextList.size()) {
			Alert alert = new Alert(ERROR);
			alert.setContentText("备份文件和源文件不匹配");
			alert.setHeaderText("错误");
			alert.showAndWait();
			return;
		}
		for (int i = 0, size = srcTextList.size(); i <size ; i++) {
			final int fi = i;
			String srcText = srcTextList.get(i).get(0);
			String transText = transTextList.get(i).get(0);
			TextField srcTextField = new TextField(srcText);
			TextField transTextField = new TextField(transText);
			Button transActionButton = new Button("翻译(有缓存)");
			Button forceTransActionButton = new Button("强制翻译");
			// 调整翻译元素组视图
			VBox transGroupVbox = new VBox();
			transGroupVbox.getChildren().add(srcTextField);
			transGroupVbox.getChildren().add(transTextField);
			HBox transBtnGroupHbox = new HBox();
			transBtnGroupHbox.getChildren().add(transActionButton);
			transBtnGroupHbox.getChildren().add(forceTransActionButton);
			transGroupVbox.getChildren().add(transBtnGroupHbox);
			transGroupVbox.getChildren().add(new Label());
			translateItemVBox.getChildren().add(transGroupVbox);
			// 调整翻译元素组视图
			List<List<String>> finalTransTextList = transTextList;
			transActionButton.setOnAction(event -> {
				try {
					String translatedText = translator.translate(srcText);
					transTextField.setText(translatedText);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			forceTransActionButton.setOnAction(event -> {
				try {
					String translatedText = translator.translateNoCache(srcText);
					transTextField.setText(translatedText);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			transTextField.textProperty().addListener((observable, oldValue, newValue) -> {
				System.out.println("oldValue = " + oldValue);
				System.out.println("newValue = " + newValue);
				finalTransTextList.get(fi).set(0, newValue);
				translator.replaceTransCache(srcText, newValue);
			});
		}

	}

	private List<List<String>> extractTextFromMap(Map<String,Object> map) throws IOException {
		List<List<String>> respList = new ArrayList<>();
		List<Map<String, Object>> events = (List<Map<String, Object>>) map.get("events");
		if (events != null && events.size() > 0) {
			for (Map<String, Object> event : events) {
				if (event != null) {
					List<Map<String, Object>> pages = (List<Map<String, Object>>) event.get("pages");
					if (pages != null && pages.size() > 0) {
						for (Map<String, Object> page : pages) {
							List<Map<String, Object>> list = (List<Map<String, Object>>) page.get("list");
							if (list != null && list.size() > 0) {
								for (Map<String, Object> stringObjectMap : list) {
									if (stringObjectMap != null) {
										if (401 == (int) stringObjectMap.get("code")) {
											List<String> parameters = (List<String>) stringObjectMap.get("parameters");
											if (parameters != null && parameters.size() == 1) {
												respList.add(parameters);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return respList;
	}


	@FXML
	public void saveMapFileActon(ActionEvent event) {
		if (transMapFileMap.size() > 0) {
			ObjectMapper objectMapper = new ObjectMapper();
			for (Map.Entry<String, Map<String, Object>> entry : transMapFileMap.entrySet()) {
				String fileName = entry.getKey();
				Map<String, Object> contentMap = entry.getValue();
				String contentString = null;
				try {
					contentString = objectMapper.writeValueAsString(contentMap);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				if (contentString != null) {
					File file = new File(workFolderPath + '\\' + fileName);
					try {
						FileUtils.write(file,contentString,"UTF-8");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

