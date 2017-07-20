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
 * Created by ESIDEM jnr on 7/20/2017.
 */

public class DbNewsAdapter extends RecyclerView.Adapter<DbNewsAdapter.NewsViewHolder>{

    // Holds on to the cursor to display the waitlist
    private Cursor mCursor;
    private Context mContext;

    public DbNewsAdapter(Context context) {
        this.mContext = context;
       //this.mCursor = cursor;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_list_yaout, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.titleView.setText(mCursor.getString(mCursor.getColumnIndex(NewsContract.Entry.COLUMN_NAME_TITLE)));
        holder.pubDate.setText(mCursor.getString(mCursor.getColumnIndex(NewsContract.Entry.COLUMN_NAME_PUBLISHED)));
        Glide.with(holder.thumbnailView.getContext()).load(mCursor.getString(mCursor.getColumnIndex(NewsContract.Entry.COLUMN_NAME_IMAGE_URL)))

                //load images as bitmaps to get fixed dimensions
                .asBitmap()

                //set a placeholder image
                .placeholder(R.mipmap.ic_launcher_round)

                //disable cache to avoid garbage collection that may produce crashes
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.thumbnailView);
    }


    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
//        if (mCursor != null) mCursor.close();
//        mCursor = newCursor;
//        if (newCursor != null) {
//            // Force the RecyclerView to refresh
//            this.notifyDataSetChanged();
//        }

        this.mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mCursor != null) {
            count = mCursor.getCount();
        }
        return count;
    }



    class NewsViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnailView;
        public TextView titleView;
        // public String favourite;
        // public TextView description;
        public TextView pubDate;
        // public TextView category;



        public NewsViewHolder(View itemView) {
            super(itemView);
            thumbnailView = (ImageView) itemView.findViewById(R.id.news_image_list);
            titleView = (TextView) itemView.findViewById(R.id.news_title_list);
            // description =(TextView) view.findViewById(R.id.card_subtitle);
            pubDate = (TextView) itemView.findViewById(R.id.news_date);
            // category = (TextView) view.findViewById(R.id.cate);
        }

    }
}
