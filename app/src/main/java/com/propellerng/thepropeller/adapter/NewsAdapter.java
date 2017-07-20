package com.propellerng.thepropeller.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.propellerng.thepropeller.R;
import com.propellerng.thepropeller.database.NewsContract;

/**
 * Created by ESIDEM jnr on 5/29/2017.
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder>{


    private final Context context;
    private Cursor cursor;

    public NewsAdapter(Context context){

        this.context = context;

    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getColumnIndex(NewsContract.Entry._ID);
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_item, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        cursor.moveToPosition(position);

        holder.titleView.setText(cursor.getString(cursor.getColumnIndex(NewsContract.Entry.COLUMN_NAME_TITLE)));
        holder.pubDate.setText(cursor.getString(cursor.getColumnIndex(NewsContract.Entry.COLUMN_NAME_PUBLISHED)));
        Glide.with(holder.thumbnailView.getContext()).load(cursor.getString(cursor.getColumnIndex(NewsContract.Entry.COLUMN_NAME_IMAGE_URL)))

                //load images as bitmaps to get fixed dimensions
                .asBitmap()

                //set a placeholder image
                .placeholder(R.mipmap.ic_launcher_round)

                //disable cache to avoid garbage collection that may produce crashes
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.thumbnailView);

    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleView;
        // public String favourite;
        // public TextView description;
        public TextView pubDate;
        // public TextView category;


        ViewHolder(View view) {
            super(view);

                thumbnailView = (ImageView) view.findViewById(R.id.horizontal_news_image);
                titleView = (TextView) view.findViewById(R.id.horizontal_news_title);
                // description =(TextView) view.findViewById(R.id.card_subtitle);
                pubDate = (TextView) view.findViewById(R.id.horizontal_news_date);
                // category = (TextView) view.findViewById(R.id.cate);
            }



    }
}
