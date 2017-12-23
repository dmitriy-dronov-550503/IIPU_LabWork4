package sample;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    // Properties:

        // View
    Button ejectButton = new Button("Eject");
    TreeView<Control> treeView;
    AnchorPane treePane = new AnchorPane();

        // Logic
    Boolean isUpdateWorking = true;

    // Get views

    public AnchorPane getRoot(){
        return treePane;
    }

    public TreeView<Control> getTreeView() {
        ArrayList<NavigableMap<Integer, LinkedList<String>>> tree = null;
        try {
            tree = parseLines();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TreeItem<Control> rootItem = new TreeItem<Control> (new MyCheckBox("Inbox").getInstance(false));
        rootItem.setExpanded(true);
        for (NavigableMap<Integer, LinkedList<String>> map : tree) {
            int key = Collections.max(map.keySet());
            int prevKey = map.lowerKey(key);
            TreeItem<Control> item = new TreeItem<Control> (new MyCheckBox(map.get(prevKey).get(0).trim()).getInstance(true));
            for (String line : map.get(key)) {
                for(int i=0; i<key; i++) System.out.print("");
                TreeItem<Control> subitem = new TreeItem<Control> (new MyCheckBox(line.trim()).getInstance(false));
                item.getChildren().add(subitem);
            }

            rootItem.getChildren().add(item);
        }
        treeView = new TreeView<>(rootItem);
        return treeView;
    }

    public Button getEjectButton(){
        ejectButton.setOnAction(e -> ejectButtonAction());
        return ejectButton;
    }

    // Initialization

    void init() throws Exception {
        Thread myThready = new Thread(new Runnable()
        {
            int lastSize=0;

            public void run()
            {
                while(isUpdateWorking){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        if(lastSize!=getLines().size()){
                            lastSize=getLines().size();
                            treeView = getTreeView();
                            treeView.refresh();
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    treePane.getChildren().clear();
                                    treePane.getChildren().add(treeView);
                                    treePane.getChildren().add(ejectButton);
                                    AnchorPane.setTopAnchor(treeView, 0.0);
                                    AnchorPane.setRightAnchor(treeView, 0.0);
                                    AnchorPane.setLeftAnchor(treeView, 0.0);
                                    AnchorPane.setBottomAnchor(treeView, 20.0);
                                    AnchorPane.setBottomAnchor(ejectButton, 0.0);
                                    AnchorPane.setLeftAnchor(ejectButton, 0.0);
                                    AnchorPane.setRightAnchor(ejectButton, 0.0);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        myThready.start();

    }

    public void stop() {
        isUpdateWorking = false;
    }

    // Private methods
    private ArrayList<String> getLines() throws Exception {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader mountOutput = null;
        try {
            Process mountProcess = Runtime.getRuntime().exec("system_profiler SPUSBDataType");
            mountOutput = new BufferedReader(new InputStreamReader(
                    mountProcess.getInputStream()));
            while (true) {
                String line = mountOutput.readLine();
                if (line == null) {
                    break;
                }
                lines.add(line);
                //(?<=MANUFACTURER:)(.*)()
                //(.*)(?=MANUFACTURER:)
                //String[] splited = line.trim().split("\\s+");
                //String mountPath = new File(line.substring(indexStart + 4, indexEnd)).getAbsolutePath();
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (mountOutput != null) {
                mountOutput.close();
            }
        }
        return lines;
    }

    private ArrayList<NavigableMap<Integer, LinkedList<String>>> parseLines() throws Exception {
        ArrayList<String> lines = getLines();
        ArrayList<NavigableMap<Integer, LinkedList<String>>> tree = new ArrayList<>();
        int prevK = 0, node = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isEmpty()) continue;
            int k = 0;
            while (line.charAt(k) == ' ') {
                k++;
            }

            if (k >= prevK) {
                prevK = k;
            } else {
                node++;
                prevK = 0;
            }

            if (node == tree.size()) {
                tree.add(new TreeMap<>());
            }
            if (!tree.get(node).containsKey(k)) {
                tree.get(node).put(k, new LinkedList<>());
            }
            tree.get(node).get(k).add(line);

        }

        for (NavigableMap<Integer, LinkedList<String>> map : tree) {
            int key = Collections.max(map.keySet());
            int prevKey = map.lowerKey(key);
            System.out.println(map.get(prevKey).get(0));
            for (String line : map.get(key)) {
                for (int i = 0; i < key; i++) System.out.print("");
                System.out.println(line);
            }
        }
        return tree;
    }

    private void ejectButtonAction() {
        ArrayList<TreeItem<Control>> itemsToRemove = new ArrayList<>();
        for (TreeItem<Control> item :
                treeView.getRoot().getChildren()) {
            CheckBox cb = (CheckBox) item.getValue();
            if (cb.isSelected()) {
                System.out.println("Selected: " + cb.getText());

                for (TreeItem<Control> subitem :
                        item.getChildren()) {

                    Label line = (Label) subitem.getValue();
                    Pattern pattern = Pattern.compile("(?<=BSD Name:)(.*)");
                    Matcher matcher = pattern.matcher(line.getText());
                    if (matcher.find()) {
                        itemsToRemove.add(item);
                        String ejectPath = matcher.group(1).trim();
                        System.out.println(cb.getText() + " will ejected by path " + ejectPath);
                        try {
                            Runtime.getRuntime().exec("diskutil eject /dev/" + ejectPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }

        for (TreeItem<Control> item :
                itemsToRemove) {
            item.getParent().getChildren().remove(item);
        }
    }

}
