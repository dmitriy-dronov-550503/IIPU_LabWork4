package sample;

import javafx.beans.property.SimpleStringProperty;

public class Device {
    private final SimpleStringProperty productName;
    private final SimpleStringProperty manufacturer;

    Device(String productName, String manufacturer) {
        this.productName = new SimpleStringProperty(productName);
        this.manufacturer = new SimpleStringProperty(manufacturer);
    }

    public String getProductName() {
        return productName.get();
    }

    public void setProductName(String fName) {
        productName.set(fName);
    }

    public String getManufacturer() {
        return manufacturer.get();
    }

    public void setManufacturer(String fName) {
        manufacturer.set(fName);
    }
}

