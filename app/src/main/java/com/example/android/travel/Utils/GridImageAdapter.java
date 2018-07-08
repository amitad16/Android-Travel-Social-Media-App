package com.example.android.travel.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.example.android.travel.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by user on 04-02-2018.
 */

public class GridImageAdapter extends ArrayAdapter<String> {

    private Context context;
    private LayoutInflater layoutInflater;
    private int layoutResource;
    private String append;
    private ArrayList<String> imgURLs;

    public GridImageAdapter(Context context, int layoutResource, String append, ArrayList<String> imgURLs) {
        super(context, layoutResource, imgURLs);
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = layoutResource;
        this.append = append;
        this.imgURLs = imgURLs;
    }

    private static class ViewHolder {
        SquareImageView squareImageView;
        ProgressBar progressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // ViewHolder build pattern similar to "RecyclerView"
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder  = new ViewHolder();
            viewHolder.progressBar = convertView.findViewById(R.id.grid_image_progress_bar);
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.squareImageView = convertView.findViewById(R.id.grid_image_view);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String imgURL = getItem(position);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, viewHolder.squareImageView, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
//                if (viewHolder.progressBar != null) {
//                    viewHolder.progressBar.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                if (viewHolder.progressBar != null) {
//                    viewHolder.progressBar.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                if (viewHolder.progressBar != null) {
//                    viewHolder.progressBar.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
//                if (viewHolder.progressBar != null) {
//                    viewHolder.progressBar.setVisibility(View.GONE);
//                }
            }
        });

        return convertView;
    }
}
