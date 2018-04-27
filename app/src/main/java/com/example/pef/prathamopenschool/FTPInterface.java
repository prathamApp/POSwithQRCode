package com.example.pef.prathamopenschool;

import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by pefpr on 31/01/2018.
 */

public interface FTPInterface {

    public interface FTPConnectInterface {
        public void onConnectionEshtablished(boolean connected);

        public void onFolderClicked(int position, String name);

        public void onDownload(int position, FTPFile name);

        public void showDialog();
    }

    public interface PushPullInterface {
        public void showDialog();

        public void onFilesRecievedComplete(String typeOfFile, String filename);
    }
}
