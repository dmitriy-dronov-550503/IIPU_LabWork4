package sample;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

public class MyCheckBox {

    private String str, devicePath="";
    private Control element;

    MyCheckBox(String str){
        super();
        this.str = str;
    }

    public void setElement(Control element) {
        this.element = element;
    }

    public Control getElement() {
        return element;
    }

    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

    public String getDevicePath() {
        return devicePath;
    }

    Control getInstance(Boolean needCheckBox){
        if(!needCheckBox){
            return new Label(str);
        }
        else{
            return new CheckBox(str);
        }
    }




}
