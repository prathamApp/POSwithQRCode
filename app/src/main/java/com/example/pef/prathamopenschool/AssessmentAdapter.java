package com.example.pef.prathamopenschool;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abc on 10-Jul-17.
 */

public class AssessmentAdapter extends ArrayAdapter<Assessment> {

    private AppCompatActivity activity;
    private List<Assessment> gameList;
    private float ratings;
    ArrayList<Integer> imageList = new ArrayList<>();

    public AssessmentAdapter(AppCompatActivity context, int ratings, List<Assessment> objects, ArrayList<Integer> imageList) {
        super(context, ratings, objects);
        this.activity = context;
        this.gameList = objects;
        this.ratings = (float) ratings;
        this.imageList = imageList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        final Assessment assessment = gameList.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.assessment_result_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
            //holder.ratingBar.getTag(position);
        }

        final float growTo = 1f;
        final long duration = 2000;
        final AnimationSet growAndShrink;
        ScaleAnimation grow = new ScaleAnimation(0, growTo, 0, growTo, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow.setDuration(duration / 2);
/*        ScaleAnimation shrink = new ScaleAnimation(growTo, 0, growTo, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(duration / 2);
        shrink.setStartOffset(duration / 2);*/
        growAndShrink = new AnimationSet(true);
        growAndShrink.setInterpolator(new LinearInterpolator());
        growAndShrink.addAnimation(grow);
//        growAndShrink.addAnimation(shrink);

//        holder.ratingBar.setRating(assessment.rating);
        holder.movieName.setText(assessment.name);
        holder.resultSmilImg.setImageResource(imageList.get(position));
        holder.ratingBar.startAnimation(growAndShrink);
        holder.resultSmilImg.startAnimation(growAndShrink);

        grow.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                float current = holder.ratingBar.getRating();

                ObjectAnimator anim = ObjectAnimator.ofFloat(holder.ratingBar, "rating", current, assessment.rating);
                anim.setDuration(1500);
                anim.start();
            }
        });


        return convertView;
    }

    private static class ViewHolder {
        private RatingBar ratingBar;
        private TextView movieName;
        ImageView resultSmilImg;

        public ViewHolder(View view) {
            ratingBar = (RatingBar) view.findViewById(R.id.RatingStars);
            movieName = (TextView) view.findViewById(R.id.GameName);
            resultSmilImg = (ImageView) view.findViewById(R.id.resultSmilImg);

        }
    }
}
