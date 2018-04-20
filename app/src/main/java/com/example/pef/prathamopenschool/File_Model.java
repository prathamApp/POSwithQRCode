package com.example.pef.prathamopenschool;

import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by PEF on 24/01/2018.
 */

public class File_Model {

    public FTPFile mfile;
    public String fileName;
    public boolean isFile;

    public FTPFile getMfile() {
        return mfile;
    }

    public void setMfile(FTPFile mfile) {
        this.mfile = mfile;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }
}
