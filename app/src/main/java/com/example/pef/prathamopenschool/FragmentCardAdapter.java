package com.example.pef.prathamopenschool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.pef.prathamopenschool.fragments.EnglishFragment;
import com.example.pef.prathamopenschool.R;

import java.io.File;
import java.util.List;

/**
 * Created by Abc on 08-Jun-17.
 */

public class FragmentCardAdapter extends RecyclerView.Adapter<FragmentCardAdapter.MyViewHolder> {

    private Context mContext;
    private List<Card> cardList;
    //VideoPlay vid;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }


    public FragmentCardAdapter(Context mContext, List<Card> cardList) {
        this.mContext = mContext;
        this.cardList = cardList;
    }

    @Override
    public FragmentCardAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contentcard, parent, false);

        return new FragmentCardAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FragmentCardAdapter.MyViewHolder holder, int position) {

        String myNodetype;

        Card card = cardList.get(position);
        final String myNodeTitle = card.nodeTitle;
        holder.title.setText(myNodeTitle);
        //holder.count.setText(card.nodeId);
        //holder.thumbnail.setImageURI( card.getThumbnail() );
        Bitmap bitmap = BitmapFactory.decodeFile(card.nodeImage);
        holder.thumbnail.setImageBitmap(bitmap);


        final String resPath = card.resourcePath;
        final String resId = card.resourceId;
        final String nodeId = card.nodeId;
        final String nodeList = card.nodeList;
        String resType = card.resourceType;
        myNodetype = card.nodeType;

        if( (myNodetype.equals("Subject")) || (myNodetype.equals("Topic")) || (myNodetype.equals("Lesson")) || (myNodetype.equals("Program")) ){

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(mContext, "Folder Clicked", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(mContext, "Dekho To Seekho", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, EnglishFragment.class);
                        intent.putExtra("nodeList", nodeList.toString());
                        ((Activity) mContext).startActivityForResult(intent, 1);
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        //Toast.makeText(mContext, "Dekho To Seekho", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, EnglishFragment.class);
                        intent.putExtra("nodeList", nodeList.toString());
                        ((Activity) mContext).startActivityForResult(intent, 1);
                }
            });
        }
        if ( resType.equals("video")) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resSrc = MainActivity.fpath + "Media/" + resPath;

                    File file = new File(MainActivity.fpath + "Media/" + resSrc);
                    Uri path = Uri.fromFile(file);
                    Intent intent = new Intent(mContext, PlayVideo.class);
                    intent.putExtra("path", path.toString());
                    ((Activity) mContext).startActivityForResult(intent, 1);

                    //resPath = MainActivity.fpath+card.resourcePath;
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resSrc = MainActivity.fpath + "Media/" + resPath;

                    File file = new File(MainActivity.fpath + "Media/" + resPath);
                    Uri path = Uri.fromFile(file);
                    Intent intent = new Intent(mContext, PlayVideo.class);
                    intent.putExtra("path", path.toString());
                    ((Activity) mContext).startActivityForResult(intent, 1);
                }
            });
        }

        if (resType.equals("game")) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resSrc = MainActivity.fpath + "Hlearning/" + resPath;

                    File file = new File(resSrc);
                    Uri path = Uri.fromFile(file);
                    Intent intent = new Intent(mContext, WebViewActivity.class);
                    intent.putExtra("path", path.toString());
                    ((Activity) mContext).startActivityForResult(intent, 1);
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resSrc = MainActivity.fpath + "HLearning/" + resPath;

                    File file = new File(resSrc);
                    Uri path = Uri.fromFile(file);
                    Intent intent = new Intent(mContext, WebViewActivity.class);
                    intent.putExtra("path", path.toString());
                    ((Activity) mContext).startActivityForResult(intent, 1);
                }
            });
        }


        // loading card cover using Glide library
//        Glide.with(mContext).load(card.getThumbnail()).into(holder.thumbnail);

    }
    @Override
    public int getItemCount() {
        return cardList.size();
    }
}
