package com.example.pef.prathamopenschool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    private ArrayList<JSONObject> mImagesList;
    private Context mContext;
    private SparseBooleanArray mSparseBooleanArray;

    public ImageAdapter(Context context, ArrayList<JSONObject> imageList) {
        mContext = context;
        mSparseBooleanArray = new SparseBooleanArray();
        mImagesList = new ArrayList<JSONObject>();
        this.mImagesList = imageList;
    }

    public ArrayList<JSONObject> getCheckedItems() {
        ArrayList<JSONObject> mTempArry = new ArrayList<JSONObject>();
        JSONObject dataForAttendance;
        try {
            for (int i = 0; i < mImagesList.size(); i++) {
                if (mSparseBooleanArray.get(i)) {
                    dataForAttendance=new JSONObject();
                    dataForAttendance.put("grpId",mImagesList.get(i).getString("grpId"));
                    dataForAttendance.put("stdId",mImagesList.get(i).getString("id"));
                    dataForAttendance.put("name",mImagesList.get(i).getString("name"));
                    mTempArry.add(dataForAttendance);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return mTempArry;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
        }
    };

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_multiphoto_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        String imageUrl = null;//mImagesList.get(position);
        try {
            imageUrl = mImagesList.get(position).getString("imgPath");
            Bitmap[] bitmap = {BitmapFactory.decodeFile(imageUrl)};
            holder.imageView.setImageBitmap(bitmap[0]);
 /*           Glide.with(mContext)
                    .load("file://"+imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .into(holder.imageView);
 */       /*String firstName = mImagesList.get(position).getString("name");
        String[] name=firstName.split("\\s");
        holder.textView.setText(name[0].substring(0, Math.min(name[0].length(), 10)));*/
            holder.textView.setText(mImagesList.get(position).getString("name"));
            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(mSparseBooleanArray.get(position));
            holder.checkBox.setOnCheckedChangeListener(mCheckedChangeListener);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mImagesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public CheckBox checkBox;
        public TextView textView;
        public ImageView imageView;

        public MyViewHolder(View view) {
            super(view);

            checkBox = (CheckBox) view.findViewById(R.id.checkBox1);
            imageView = (ImageView) view.findViewById(R.id.imageView1);
            textView = (TextView) view.findViewById(R.id.groupName);
        }
    }

}
