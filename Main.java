package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.usb4java.*;


public class Main extends Application {

    Controller controller = new Controller();

    @Override
    public void start(Stage primaryStage) throws Exception{

        controller.init();
        AnchorPane treePane = controller.getRoot();
        TreeView<Control> treeView = controller.getTreeView();
        Button ejectButton = controller.getEjectButton();
        Label loadingText = new Label("Loading...");
        AnchorPane.setLeftAnchor(loadingText, 250.0);
        AnchorPane.setTopAnchor(loadingText, 150.0);
        treePane.getChildren().add(loadingText);

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(treePane, 500, 300));
        primaryStage.show();
    }


    @Override
    public void stop(){
        controller.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


