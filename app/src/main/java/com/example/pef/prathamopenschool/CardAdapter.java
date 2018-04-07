package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

/**
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {

    public static Context mContext;
    private List<Card> cardList;
    ScoreDBHelper sdb;
    PDFTracking pdt;
    public TextView bread_crums;
    static String resId, nodeDesc, resName;
    static boolean pdfFlag = false, vidFlg = false, newAssessmentFlg;
    Boolean LangFlag = false;
    static String pdfStartTime;
    //VideoPlay vid;
    Utility util = new Utility();
    int langFlg = 0;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, phase, count, bread_crums;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            phase = (TextView) view.findViewById(R.id.phase);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

            // Setting font based on language
            if ((MultiPhotoSelectActivity.getLanguage(mContext)).equals("Punjabi")) {
                langFlg = 1;
                Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/raavi_punjabi.ttf");
                title.setTypeface(custom_font, Typeface.BOLD);
            } else if ((MultiPhotoSelectActivity.getLanguage(mContext)).equals("Odiya")) {
                langFlg = 2;
                Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/lohit_oriya.ttf");
                title.setTypeface(custom_font, Typeface.BOLD);
            } else if ((MultiPhotoSelectActivity.getLanguage(mContext)).equals("Gujarati")) {
                langFlg = 3;
                Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/muktavaani_gujarati.ttf");
                title.setTypeface(custom_font, Typeface.BOLD);
            }
            /*else if(MultiPhotoSelectActivity.language.equals("Assamese")){
                Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(),  "fonts/geetl_assamese.ttf");
                title.setTypeface(custom_font,Typeface.BOLD);
            }
            else if(MultiPhotoSelectActivity.language.equals("Assamese")){
                Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(),  "fonts/verdana_english.ttf");
                title.setTypeface(custom_font,Typeface.BOLD);
            }*/

        }
    }


    public CardAdapter(Context mContext, List<Card> cardList) {
        this.mContext = mContext;
        this.cardList = cardList;
        sdb = new ScoreDBHelper(mContext);
        pdt = new PDFTracking(sdb);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contentcard, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        String myNodetype;

        Card card = cardList.get(position);
        final String myNodeTitle = card.nodeTitle;

        char ch = card.nodeTitle.charAt(0);
        int ascii = ch;

        if ((MultiPhotoSelectActivity.getLanguage(mContext)).equals("Assamese")) {
            if ((ascii > 96 && ascii < 123) || (ascii > 64 && ascii < 91) || (ascii > 47 && ascii < 58)) {
                // set font english
                Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/verdana_english.ttf");
                holder.title.setTypeface(custom_font, Typeface.BOLD);

            } else {
                Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/geetl_assamese.ttf");
                holder.title.setTypeface(custom_font, Typeface.BOLD);
            }
        }

        System.out.println("---------------------------------------------LANGUAGE - " + MultiPhotoSelectActivity.getLanguage(mContext));

        holder.title.setText(myNodeTitle);
        holder.phase.setText(card.nodePhase);
        //holder.count.setText(card.nodeId);
        //holder.thumbnail.setImageURI( card.getThumbnail() );
        /*final Bitmap[] bitmap = {BitmapFactory.decodeFile(card.nodeImage)};
        holder.thumbnail.setImageBitmap(bitmap[0]);*/

        Glide.with(holder.itemView.getContext())
                .load("file://" + card.nodeImage)
                .into(holder.thumbnail);

        final String resPath = card.resourcePath;
        final String tempNodeDesc = card.nodeDesc;
        final String tempresId = card.resourceId;
        final String nodeId = card.nodeId;
        final String nodeList = card.nodeList;
        final String tempResName = card.nodeTitle;
        String resType = card.resourceType;
        myNodetype = card.nodeType;

        if ( myNodetype.equals("Assessment") ) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newAssessmentFlg = true;
                    Intent mainNew = new Intent(mContext, assessmentLogin.class);
                    Runtime rs = Runtime.getRuntime();
                    rs.gc();
                    rs.freeMemory();
                    mainNew.putExtra("nodeList", nodeList.toString());
                    ((Activity) mContext).startActivityForResult(mainNew, 1);
                    //resPath = MainActivity.fpath+card.resourcePath;
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newAssessmentFlg = true;
                    Intent mainNew = new Intent(mContext, assessmentLogin.class);
                    Runtime rs = Runtime.getRuntime();
                    rs.gc();
                    rs.freeMemory();
                    mainNew.putExtra("nodeList", nodeList.toString());
                    ((Activity) mContext).startActivityForResult(mainNew, 1);

                }
            });
        } else if ((myNodetype.equals("Statistics")) && (myNodeTitle.equals("Tab Statistics"))) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, AssessmentCrlDashBoardView.class);
                    Runtime rs = Runtime.getRuntime();
                    i.putExtra("fromActivity", "main");
                    rs.gc();
                    rs.freeMemory();
                    ((Activity) mContext).startActivityForResult(i, 1);
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, AssessmentCrlDashBoardView.class);
                    Runtime rs = Runtime.getRuntime();
                    i.putExtra("fromActivity", "main");
                    rs.gc();
                    rs.freeMemory();
                    ((Activity) mContext).startActivityForResult(i, 1);
                }
            });
        } else if ((myNodetype.equals("Subject")) || (myNodetype.equals("Topic")) || (myNodetype.equals("Lesson")) || (myNodetype.equals("Program"))) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    String crums = myNodeTitle;
                    intent.putExtra("nodeList", nodeList.toString());
                    intent.putExtra("crums", crums);
                    Runtime rs = Runtime.getRuntime();
                    rs.freeMemory();
                    rs.gc();
                    rs.freeMemory();
                    ((Activity) mContext).startActivityForResult(intent, 1);
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    String crums = myNodeTitle;
                    intent.putExtra("nodeList", nodeList.toString());
                    intent.putExtra("crums", crums);
                    Runtime rs = Runtime.getRuntime();
                    rs.freeMemory();
                    rs.gc();
                    rs.freeMemory();
                    ((Activity) mContext).startActivityForResult(intent, 1);
                }
            });
        }
        if (resType.equals("video")) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vidFlg = true;
                    String resSrc = splashScreenVideo.fpath + "Media/" + resPath;
                    resId = tempresId;
                    File file = new File(resSrc);
                    Uri path = Uri.fromFile(file);
                    String myDateTime = util.GetCurrentDateTime(false);
                    if (file.exists()) {
                        Intent intent = new Intent(mContext, PlayVideo.class);
                        intent.putExtra("path", path.toString());
                        intent.putExtra("startTime", myDateTime);
                        Runtime rs = Runtime.getRuntime();
                        rs.freeMemory();
                        rs.gc();
                        rs.freeMemory();
                        ((Activity) mContext).startActivityForResult(intent, 1);
                        JSInterface.MediaFlag = true;
                    } else {
                        Toast.makeText(mContext, "Video not available !!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vidFlg = true;
                    String resSrc = splashScreenVideo.fpath + "Media/" + resPath;
                    resId = tempresId;
                    File file = new File(resSrc);
                    Uri path = Uri.fromFile(file);
                    String myDateTime = util.GetCurrentDateTime(false);
                    if (file.exists()) {
                        Intent intent = new Intent(mContext, PlayVideo.class);
                        intent.putExtra("path", path.toString());
                        intent.putExtra("startTime", myDateTime);
                        Runtime rs = Runtime.getRuntime();
                        rs.freeMemory();
                        rs.gc();
                        rs.freeMemory();
                        ((Activity) mContext).startActivityForResult(intent, 1);
                        JSInterface.MediaFlag = true;
                    } else {
                        Toast.makeText(mContext, "Video not available !!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (resType.equals("pdf")) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resSrc = splashScreenVideo.fpath + "Media/" + resPath;
                    resId = tempresId;
                    try {
                        File file = new File(resSrc);

                        if (file.exists()) {
                            Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "application/pdf");
                            try {
                                ((Activity) mContext).startActivityForResult(intent, 2);
                                pdfFlag = true;
                                PDFTracking.calculateStartTime();
                            } catch (ActivityNotFoundException e) {
                                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                                syncActivityLogs.addToDB("showPdf-JSInterface", e, "Error");
                            }
                        } else {
                            Toast.makeText(mContext, "pdf not available !!!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                    }
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resSrc = splashScreenVideo.fpath + "Media/" + resPath;
                    resId = tempresId;
                    try {
                        File file = new File(resSrc);

                        if (file.exists()) {
                            Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "application/pdf");
                            try {
                                ((Activity) mContext).startActivityForResult(intent, 2);
                                pdfFlag = true;
                                PDFTracking.calculateStartTime();
                            } catch (ActivityNotFoundException e) {
                                SyncActivityLogs syncActivityLogs = new SyncActivityLogs(mContext);
                                syncActivityLogs.addToDB("showPdf-JSInterface", e, "Error");
                            }
                        } else {
                            Toast.makeText(mContext, "pdf not available !!!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
        if (resType.equals("game")) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resSrc = splashScreenVideo.fpath + resPath;
                    resId = tempresId;
                    nodeDesc = tempNodeDesc;
                    resName = tempResName;
                    File file = new File(resSrc);
                    Uri path = Uri.fromFile(file);

                    if (file.exists()) {
                        Intent intent = new Intent(mContext, WebViewActivity.class);
                        intent.putExtra("path", path.toString());
                        intent.putExtra("resId", resId);
                        intent.putExtra("resName", resName);
                        Runtime rs = Runtime.getRuntime();
                        rs.freeMemory();
                        rs.gc();
                        rs.freeMemory();
                        ((Activity) mContext).startActivityForResult(intent, 1);
                    } else {
                        Toast.makeText(mContext, "Game not available !!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resSrc = splashScreenVideo.fpath + resPath;
                    resId = tempresId;
                    nodeDesc = tempNodeDesc;
                    resName = tempResName;
                    File file = new File(resSrc);
                    Uri path = Uri.fromFile(file);

                    if (file.exists()) {
                        Intent intent = new Intent(mContext, WebViewActivity.class);
                        intent.putExtra("path", path.toString());
                        intent.putExtra("resId", resId);
                        intent.putExtra("resName", resName);
                        Runtime rs = Runtime.getRuntime();
                        rs.freeMemory();
                        rs.gc();
                        rs.freeMemory();
                        ((Activity) mContext).startActivityForResult(intent, 1);
                    } else {
                        Toast.makeText(mContext, "Game not available !!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}
