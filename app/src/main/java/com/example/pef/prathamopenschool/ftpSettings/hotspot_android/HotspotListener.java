package com.example.pef.prathamopenschool.ftpSettings.hotspot_android;

import java.util.ArrayList;

public interface HotspotListener {
    /**
     * Interface called when the scan method finishes. Network operations should not execute on UI thread
     *
     * @param clients
     */
    void OnDevicesConnectedRetrieved(ArrayList<ConnectedDevice> clients);

    void OnHotspotStartResult(ConnectionResult result);
}