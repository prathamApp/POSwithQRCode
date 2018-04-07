package com.example.pef.prathamopenschool;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.example.pef.prathamopenschool.FileUtil.getExtSdCardFolder;

/**
 * Created by PEF on 04/10/2017.
 */

class UnZipTask extends AsyncTask<String, String, Boolean> {
    String zipPath;
    private String destinationPath;
    Dialog dialog;
    ProgressBar progressBar;
    TextView file_name;
    Context context;
    private Uri treeUri;
    ArrayList<ZipEntry> arrayList = new ArrayList<>();
    private DocumentFile zipd;
    long ttl_size = 0L;
    long totalBytesCopied = 0L;
    private String configFilePath;


    public UnZipTask(Context context, String zipPath/*, String targetPath, ExtractInterface extractInterface*/) {
        this.zipPath = zipPath;
        this.context = context;
        Log.d("zipPath::", zipPath);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.copy_progress_bar);
        dialog.setTitle("Extracting...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        progressBar = (ProgressBar) dialog.findViewById(R.id.extract_progressBar);
        file_name = (TextView) dialog.findViewById(R.id.f_name);
        dialog.show();
    }


    @SuppressWarnings("rawtypes")
    @Override
    protected Boolean doInBackground(String... params) {
        destinationPath = PreferenceManager.getDefaultSharedPreferences(context).getString("PATH", null);
        treeUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("URI", null));
        DocumentFile pickedDir = DocumentFile.fromTreeUri(context, treeUri);
        zipd = pickedDir.findFile("POSexternal.zip");
        unzipToSDcard(zipd);
        return true;
    }

    /*
    for extracting zip to sdcard or mount storage
     */
    public boolean unzipToSDcard(DocumentFile zipFile/*, File destinationDir*/) {
        ZipFile zip = null;
        try {
            String path = SDCardUtil.getRealPathFromURI(context, zipFile.getUri());
            zip = new ZipFile(path);
            for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
                // adding all the elements to be extracted to an array list
                ZipEntry entry = (ZipEntry) e.nextElement();
                ttl_size += entry.getSize();
                arrayList.add(entry);
            }
            for (ZipEntry z_entry : arrayList) {
                unzipEntry(zip, z_entry, destinationPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
        return true;
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws Exception {
        if (entry.isDirectory()) {
            getDocumentFile(new File(outputDir, "/" + entry.getName()), true, context);
            return;
        }
        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            getDocumentFile(outputFile.getParentFile(), true, context);
        }
        DocumentFile df = getDocumentFile(outputFile, false, context);
        // Copy new Config file to Internal
        if (outputFile.getAbsolutePath().endsWith("Config.json")) {
            configFilePath = outputFile.getAbsolutePath();
            Log.d("configFilePath:::", configFilePath);
        }
        InputStream inputStream = zipfile.getInputStream(entry);
        FileOutputStream outputStream = (FileOutputStream) context.getContentResolver().openOutputStream(df.getUri());
        try {
            int len = 0;
            byte buf[] = new byte[GenericCopyUtil.DEFAULT_BUFFER_SIZE];
            while ((len = inputStream.read(buf, 0, buf.length)) > 0) {
                outputStream.write(buf, 0, len);
                totalBytesCopied += len;
                int progress = (int) Math.round(((double) totalBytesCopied / (double) ttl_size) * 100);
                publishProgress(entry.getName(), "" + progress);
            }
        } catch (IOException e) {
            e.printStackTrace();
            outputStream.close();
        } finally {
//            outputStream.flush();
//            outputStream.close();
//            inputStream.close();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        dialog.setTitle("Extracting...." + values[1] + "%");
        file_name.setText(values[0]);
        progressBar.setProgress(Integer.parseInt(values[1]));
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
            // Delete POSexternal.zip
            try {
                File file = new File(zipPath);
                boolean deleted = file.delete();
                if (deleted) Log.d("file:::", "deleted");
                if (zipd != null) {
                    boolean del = zipd.delete();
                    if (del) Log.d("document_file:::", "deleted");
                }
                // Copy Config in Internal Storage
//                File extConfigPath = new File(splashScreenVideo.fpath + "toCopy/.POSinternal/Json/Config.json");
                File intConfigPath = new File(Environment.getExternalStorageDirectory().toString() + "/.POSinternal/Json/Config.json");
                if (!new File(configFilePath).exists()) {
                    Toast.makeText(context, "Updated Config.json not found in extSDCard !!!", Toast.LENGTH_LONG).show();
                } else {
                    // If new Config found
                    try {
                        copyFile(new File(configFilePath), intConfigPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(context, "Media Updated Successfully !!!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
//            progressUpdate.onZipExtracted(false);
        }
    }

    //Copy Dir Function for Config
    private void copyFile(File source, File target) throws IOException {
        try {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

//    private void createDir(File dir) {
//        FileUtil.mkdir(dir, context);
//    }

    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file, context);
//        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            //continue
        }

        String as = PreferenceManager.getDefaultSharedPreferences(context).getString("URI", null);

        Uri treeUri = null;
        if (as != null) treeUri = Uri.parse(as);
        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
//        if (originalDirectory) return document;
        Log.d("path:::", relativePath);
        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);
            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }
        return document;
    }

}

    /*
    for extracting zip to internal memory
     */
//    public static boolean unzip(File zipFile, File destinationDir) {
//        ZipFile zip = null;
//        try {
//            if (!destinationDir.exists()) {
//                destinationDir.mkdirs();
//            }
//            zip = new ZipFile(zipFile);
//            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
//            while (zipFileEntries.hasMoreElements()) {
//                ZipEntry entry = zipFileEntries.nextElement();
//                String entryName = entry.getName();
//                File destFile = new File(destinationDir, entryName);
//                File destinationParent = destFile.getParentFile();
//                Log.d("destFile:::", destFile.getAbsolutePath());
//                Log.d("destinationParent:::", destinationParent.getAbsolutePath());
//                if (destinationParent != null && !destinationParent.exists()) {
//                    destinationParent.mkdirs();
//                }
//                if (!entry.isDirectory()) {
//                    BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
//                    int currentByte;
//                    byte data[] = new byte[8192];
//                    FileOutputStream fos = new FileOutputStream(destFile);
//                    BufferedOutputStream dest = new BufferedOutputStream(fos, 8192);
//                    while ((currentByte = is.read(data, 0, 8192)) != EOF) {
//                        dest.write(data, 0, currentByte);
//                    }
//                    dest.flush();
//                    dest.close();
//                    is.close();
//                }
//            }
//        } catch (Exception e) {
//            return false;
//        } finally {
//            if (zip != null) {
//                try {
//                    zip.close();
//                } catch (IOException ignored) {
//                }
//            }
//        }
//        return true;
//    }


//            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
//            while (zipFileEntries.hasMoreElements()) {
//                ZipEntry entry = zipFileEntries.nextElement();
//                String entryName = entry.getName();
//                tempDirectory = tempDirectory.findFile(entryName);
//                if (entry.isDirectory()) {
//                    if (tempDirectory == null) {
//                        tempDirectory = tempDirectory.createDirectory(entryName);
//                    }
//                } else {
//                    tempDirectory = tempDirectory.createFile(entry.ge);
//                    BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
//                    int currentByte;
//                    byte data[] = new byte[8192];
////                    FileOutputStream fos = new FileOutputStream(tempDirectory.getUri());
//                    OutputStream fos = context.getContentResolver().openOutputStream(tempDirectory.getUri());
//                    BufferedOutputStream dest = new BufferedOutputStream(fos, 8192);
//                    while ((currentByte = is.read(data, 0, 8192)) != EOF) {
//                        dest.write(data, 0, currentByte);
//                    }
//                    dest.flush();
//                    dest.close();
//                    is.close();
////                }
//                }
////                File destFile = new File(destinationDir, entryName);
////                File destinationParent = destFile.getParentFile();
////                Log.d("destFile:::", destFile.getAbsolutePath());
////                Log.d("destinationParent:::", destinationParent.getAbsolutePath());
////                if (destinationParent != null && !destinationParent.exists()) {
////                    destinationParent.mkdirs();
////                }
////                if (!entry.isDirectory()) {
//            }
