package com.example.pef.prathamopenschool;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;

import com.example.pef.prathamopenschool.interfaces.ExtractInterface;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class CopyFiles extends AsyncTask<Void, Integer, String> {

    Context context;
    File sourcePath;
    File targetPath;
    ProgressDialog dialog;
    String zipPath;
    private ExtractInterface extractInterface;

    public CopyFiles(String zipPath, Context context, ExtractInterface extractInterface) {
        this.context = context;
        this.zipPath = zipPath;
        this.extractInterface = extractInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage("Copying...");
        dialog.setMax(100);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    @Override
    protected String doInBackground(Void... voids) {
        String uri = PreferenceManager.getDefaultSharedPreferences(context).getString("URI", "");
        DocumentFile pickedDir = DocumentFile.fromTreeUri(context, Uri.parse(uri));
        DocumentFile tmp = pickedDir.findFile("POSexternal.zip");
        if (tmp != null) {
            tmp.delete();
        }
        DocumentFile nfile = pickedDir.createFile("application/zip", new File(zipPath).getName());
        long ttl_size = new File(zipPath).length();
        try {
//            IOUtils.copyLarge(new FileInputStream(new File(zipPath)), context.getContentResolver().openOutputStream(nfile.getUri()));
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(zipPath)));
            BufferedOutputStream outputStream = new BufferedOutputStream(context.getContentResolver().openOutputStream(nfile.getUri()));
            try {
                int len;
                long totalBytesCopied = 0;
                byte buf[] = new byte[GenericCopyUtil.DEFAULT_BUFFER_SIZE];
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                    totalBytesCopied += len;
                    int progress = (int) Math.round(((double) totalBytesCopied / (double) ttl_size) * 100);
                    publishProgress(progress);
                }
            } finally {
                outputStream.flush();
                outputStream.close();
//            zipfile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (dialog != null) {
            dialog.setProgress(values[0]);
        }

    }

    @Override
    protected void onPostExecute(String result) {
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
            extractInterface.onExtractDone(zipPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


//                BufferedInputStream in = null;
//                BufferedOutputStream out = null;
//                ReadableByteChannel inputChannel = null;
//                WritableByteChannel outputChannel = null;
//                FileChannel inputChannel = null;
//                FileChannel outputChannel= null;
//                try {
//                    FileUtils.copyFile(sourcePath,context.getContentResolver().openOutputStream(copy1.getUri()));
//                    Files.copy(sourcePath,context.getContentResolver().openOutputStream(copy1.getUri()));
//                    inputChannel = new FileInputStream(sourcePath).getChannel();
//                    FileOutputStream fileOutputStream = (FileOutputStream) context.getContentResolver().openOutputStream(copy1.getUri());
//                    outputChannel = fileOutputStream.getChannel();
//                    int maxCount = (64 * 1024 * 1024) - (32 * 1024);
//                    long size = inputChannel.size();
//                    long position = 0;
//                    while (position < size) {
//                        position += inputChannel.transferTo(position, maxCount, outputChannel);
//                    }
//                    InputStream input = new FileInputStream(sourcePath);
//                    OutputStream output = context.getContentResolver().openOutputStream(copy1.getUri());
//                    inputChannel = Channels.newChannel(input);
//                    outputChannel = Channels.newChannel(output);
//                    fastChannelCopy(inputChannel, outputChannel);

//                    in = new BufferedInputStream(context.getContentResolver().openInputStream(DocumentFile.fromFile(sourcePath).getUri()));
//                    out = new BufferedOutputStream(context.getContentResolver().openOutputStream(copy1.getUri()));
//                    byte[] buffer = new byte[40000];
//                    int bytesRead = 0;
//                    while ((bytesRead = in.read(buffer, 0, 40000)) != -1) {
//                        out.write(buffer, 0, bytesRead);
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        inputChannel.close();
//                        outputChannel.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }


//    public void copyDirectoryToSdCard(File sourcePath, File targetPath, DocumentFile t_Uri) {
//        try {
//            Uri treeUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("URI", null));
//            if (sourcePath.isDirectory()) {
//                String baseFolder = FileUtil.getExtSdCardFolder(targetPath, context);
//                String[] folders = targetPath.getAbsolutePath().replaceFirst(baseFolder + "/", "").split("/");
//                DocumentFile pickedDir = DocumentFile.fromTreeUri(context, treeUri);
//                if (!targetPath.exists()) {
//                    // Create Directory using Tree ( like mkdir )
//                    DocumentFile tempDirectory = pickedDir;
//                    for (int i = 0; i < folders.length - 1; i++) {
//                        for (DocumentFile dir : pickedDir.listFiles()) {
//                            if (dir.getName() != null && dir.isDirectory()) {
//                                if (dir.getName().equals(folders[i])) {
//                                    tempDirectory = dir;
//                                    break;
//                                }
//                            }
//                        }
//                        pickedDir = tempDirectory;
//                    }
//                    t_Uri = pickedDir.createDirectory(folders[folders.length - 1]);
//                } else {
//                    // Replace Files ( if already exists )
//                    for (int i = 0; i < folders.length; i++) {
//                        pickedDir = pickedDir.findFile(folders[i]);
//                        if (pickedDir != null) {
//                            t_Uri = pickedDir;
//                        }
//                    }
//                }
//                for (String file : sourcePath.list()) {
//                    copyDirectoryToSdCard(new File(sourcePath, file), new File(targetPath, file), t_Uri);
//                }
//            } else {
//                String mime = mime(sourcePath.toURI().toString());
//                DocumentFile copy1;
//                if (t_Uri != null) {
//                    if (t_Uri.findFile(sourcePath.getName()) != null) {
//                        copy1 = t_Uri.findFile(sourcePath.getName());
//                        copy1.delete();
//                    }
//                }
//                copy1 = t_Uri.createFile(mime, sourcePath.getName());
//                IOUtils.copyLarge(new FileInputStream(sourcePath), context.getContentResolver().openOutputStream(copy1.getUri()));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
////    public static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
////        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
////        while (src.read(buffer) != -1) {
////            // prepare the buffer to be drained
////            buffer.flip();
////            // write to the channel, may block
////            dest.write(buffer);
////            // If partial transfer, shift remainder down
////            // If buffer is empty, same as doing clear()
////            buffer.compact();
////        }
////        // EOF will leave buffer in fill state
////        buffer.flip();
////        // make sure the buffer is fully drained.
////        while (buffer.hasRemaining()) {
////            dest.write(buffer);
////        }
////    }
//
//    public String mime(String URI) {
//        String type = null;
//        String extention = MimeTypeMap.getFileExtensionFromUrl(URI);
//        if (extention != null) {
//            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
//        }
//        if (type == null) {
//            if (getFileExt(URI).equalsIgnoreCase("js"))
//                type = "text/js";
//            else if (getFileExt(URI).equalsIgnoreCase("json"))
//                type = "text/json";
//            else if (getFileExt(URI).equalsIgnoreCase("txt"))
//                type = "text/txt";
//            else if (getFileExt(URI).equalsIgnoreCase("woff") || getFileExt(URI).equalsIgnoreCase("ttf")
//                    || getFileExt(URI).equalsIgnoreCase("otf") || getFileExt(URI).equalsIgnoreCase("woff2")
//                    || getFileExt(URI).equalsIgnoreCase("eot"))
//                type = "application/octet-stream";
//        }
//        Log.d("mime_type:::", type);
//        return type;
//    }
//
//    public static String getFileExt(String fileName) {
//        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
//    }
