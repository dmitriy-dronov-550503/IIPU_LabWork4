/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt for licensing information.
 */

package sample;

import org.usb4java.*;
import org.usb4java.Device;

import javax.usb.UsbHostManager;
import javax.usb.UsbServices;
import java.util.ArrayList;

/**
 * Demonstrates how to use the hotplug feature of libusb.
 * 
 * @author Klaus Reimer <k@ailis.de>
 */
public class HotPlug
{



    static class EventHandlingThread extends Thread
    {
        private volatile boolean abort;

        public void abort()
        {
            this.abort = true;
        }

        @Override
        public void run()
        {
            while (!this.abort)
            {
                int result = LibUsb.handleEventsTimeout(null, 1000000);
                if (result != LibUsb.SUCCESS)
                    throw new LibUsbException("Unable to handle events", result);
            }
        }
    }


    static EventHandlingThread thread;
    static HotplugCallbackHandle callbackHandle;

    public void run(HotplugCallback callback) throws Exception
    {
        int result = LibUsb.init(null);

        if (result != LibUsb.SUCCESS)
        {
            throw new LibUsbException("Unable to initialize libusb", result);
        }

        if (!LibUsb.hasCapability(LibUsb.CAP_HAS_HOTPLUG))
        {
            System.err.println("libusb doesn't support hotplug on this system");
            System.exit(1);
        }

        thread = new EventHandlingThread();
        thread.start();

        callbackHandle = new HotplugCallbackHandle();
        result = LibUsb.hotplugRegisterCallback(null,
                LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED
                        | LibUsb.HOTPLUG_EVENT_DEVICE_LEFT,
            LibUsb.HOTPLUG_ENUMERATE,
            LibUsb.HOTPLUG_MATCH_ANY,
            LibUsb.HOTPLUG_MATCH_ANY,
            LibUsb.HOTPLUG_MATCH_ANY,
            callback, null, callbackHandle);
        if (result != LibUsb.SUCCESS)
        {
            throw new LibUsbException("Unable to register hotplug callback",
                result);
        }
    }

    void stop() throws Exception{
        // Unregister the hotplug callback and stop the event handling thread
        thread.abort();
        LibUsb.hotplugDeregisterCallback(null, callbackHandle);
        thread.join();

        // Deinitialize the libusb context
        LibUsb.exit(null);
    }
}
