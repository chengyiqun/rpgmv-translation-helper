package com.cyq.rpgmvtranshelper;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * @author cheng
 */
public class MainClass extends javafx.application.Application {

	public static void main(String[] args) {
		MainClass.launch(args);
	}

//	@Override
//	public void start(Stage primaryStage) throws Exception {
//
//		// 这里的root从FXML文件中加载进行初始化，这里FXMLLoader类用于加载FXML文件
//		AnchorPane root = FXMLLoader.load(getClass().getResource("/MainPanel.fxml"));
//		Scene scene = new Scene(root, 800, 600);
//		// set title
//		primaryStage.setTitle("Hello World! 你好世界。");
//		// set icon
//		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon/icon.png")));
//		primaryStage.setScene(scene);
//		primaryStage.setResizable(false);
//		primaryStage.show();
//	}


	@Override
	public void start(Stage primaryStage) throws Exception {
		Node rootIcon = new ImageView(
				new Image(getClass().getResourceAsStream("/img/icon/icon.png"))
		);
		primaryStage.setTitle("Tree View Sample");

		// TreeItem名字和图标
		TreeItem<String> rootItem = new TreeItem<> ("Inbox", rootIcon);
		rootItem.setExpanded(true);
		// 每个Item下又可以添加新的Item
		for (int i = 1; i < 6; i++) {
			TreeItem<String> item = new TreeItem<> ("Message" + i);
			item.getChildren().add(new TreeItem<String>("第三级"));
			rootItem.getChildren().add(item);
			
		}
		// 创建TreeView
		TreeView<String> tree = new TreeView<> (rootItem);

		StackPane root = new StackPane();
		root.getChildren().add(tree);
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.show();

		tree.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			Node lastNode;
			public void handle(MouseEvent event)
			{
				Node node = event.getPickResult().getIntersectedNode();
				if ((node != lastNode)) {
					if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
						String name = (String) ((TreeItem)tree.getSelectionModel().getSelectedItem()).getValue();
						System.out.println("Node click: " + name);
					}
				}
				lastNode = node;
			}
		});
	}

}
