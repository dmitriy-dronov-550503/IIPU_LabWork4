/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt for licensing information.
 */

package sample;

import javafx.util.Pair;
import org.usb4java.Context;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.Transfer;

import javax.usb.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Dumps the names of all USB devices by using the javax-usb API. On
 * Linux this can only work when your user has write permissions on all the USB
 * device files in /dev/bus/usb (Running this example as root will work). On
 * Windows this can only work for devices which have a libusb-compatible driver
 * installed. On OSX this usually works without problems.
 * 
 * @author Klaus Reimer <k@ailis.de>
 */
public class DumpNames
{

    static ArrayList<HashMap<String, String>> devices = new ArrayList<>();

    public static ArrayList<HashMap<String,String>> processDeviceCompare(final UsbDevice device, ArrayList<Pair<Short, Short>> vendors)
    {
        // When device is a hub then process all child devices
        if (device.isUsbHub())
        {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child: (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                processDeviceCompare(child, vendors);
            }
        }

        // When device is not a hub then dump its name.
        else
        {
            try
            {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if(vendors.contains(new Pair(desc.idVendor(), desc.idProduct()))){
                    devices.add(getDeviceInformation(device));
                    if(desc.idVendor()==0x0930){
                        DeviceHandle deviceHandle = LibUsb.openDeviceWithVidPid(null, desc.idVendor(), desc.idProduct());
                        if(deviceHandle==null){
                            System.out.println("Can't open device handle.");
                        }
                        else{
                            // Help to get data
                            // System.out.println("O: "+LibUsb.getStringDescriptor(deviceHandle, (byte)12));
                            Transfer transfer = LibUsb.allocTransfer();
                            //System.out.println("O: "+LibUsb.cancelTransfer(transfer));

                            LibUsb.close(deviceHandle);
                        }

                    }
                }
            }
            catch (Exception e)
            {
                // On Linux this can fail because user has no write permission
                // on the USB device file. On Windows it can fail because
                // no libusb device driver is installed for the device
                System.err.println("Ignoring problematic device: " + e);
            }
        }
        return devices;
    }

    public static ArrayList<HashMap<String,String>> processDevice(final UsbDevice device)
    {
        // When device is a hub then process all child devices
        if (device.isUsbHub())
        {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child: (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                processDevice(child);
            }
        }

        // When device is not a hub then dump its name.
        else
        {
            try
            {
                devices.add(getDeviceInformation(device));
            }
            catch (Exception e)
            {
                // On Linux this can fail because user has no write permission
                // on the USB device file. On Windows it can fail because
                // no libusb device driver is installed for the device
                System.err.println("Ignoring problematic device: " + e);
            }
        }
        return devices;
    }

    private static HashMap<String, String> getDeviceInformation(final UsbDevice device) throws UnsupportedEncodingException, UsbException {
        HashMap<String, String> deviceInfo = new HashMap<>();
        final UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
        final byte iManufacturer = desc.iManufacturer();
        final byte iProduct = desc.iProduct();
        final short f = desc.bcdUSB();
        if (iManufacturer == 0 || iProduct == 0) return null;

        deviceInfo.put("product", device.getString(iProduct));
        deviceInfo.put("manufacturer", device.getString(iManufacturer));

        return deviceInfo;
    }

}
