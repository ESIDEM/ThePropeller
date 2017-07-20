package com.propellerng.thepropeller.fragments;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.propellerng.thepropeller.adapter.DbNewsAdapter;
import com.propellerng.thepropeller.adapter.NewsAdapter;
import com.propellerng.thepropeller.R;
import com.propellerng.thepropeller.database.NewsContract;
import com.propellerng.thepropeller.database.NewsDBHelper;
import com.propellerng.thepropeller.sync.NewsSyncJobs;
import com.propellerng.thepropeller.util.DividerItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentPost extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    public RecentPost() {
        // Required empty public constructor
    }

    private SQLiteDatabase mDb;
    private DbNewsAdapter politicsNewsAdapter;
    private DbNewsAdapter businessNewsAdapter;
    private static final int STOCK_LOADER = 0;
    private RecyclerView mRecyclerView;
    private RecyclerView politics_recyclerView;
    private RecyclerView business_recyclerView;
    //private SwipeRefreshLayout mSwipRefresh;
    private TextView mErrorTextView;
    private NewsAdapter mAdapter;
    private LinearLayoutManager linearLayoutManager;
    private LinearLayoutManager linearLayoutManagerbusiness;
    String[] PROJECTION = {
            NewsContract.Entry._ID,
            NewsContract.Entry.COLUMN_NAME_TITLE,
            NewsContract.Entry.COLUMN_NAME_PUBLISHED,
            NewsContract.Entry.COLUMN_NAME_LINK,
            NewsContract.Entry.COLUMN_NAME_DESCRIPTION,
            NewsContract.Entry.COLUMN_NAME_IMAGE_URL,
            NewsContract.Entry.COLUMN_NAME_FAV,

    };

    Cursor politicsCursor;
    Cursor businessCursor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent_post, container, false);

        NewsDBHelper dbHelper = new NewsDBHelper(getActivity());
        mDb = dbHelper.getWritableDatabase();
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view_latest);
        politics_recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_politics);
        business_recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_business);
       // mSwipRefresh = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        mErrorTextView = (TextView)view.findViewById(R.id.error);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManagerbusiness = new LinearLayoutManager(getActivity());
        mAdapter = new NewsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(layoutManager);

//        mSwipRefresh.setOnRefreshListener(this);
//        mSwipRefresh.setRefreshing(true);

        NewsSyncJobs.initialize(getActivity());
        onRefresh();
        getLoaderManager().initLoader(STOCK_LOADER, null, this);

        // Get  all Business News from the database and save in a cursor

        politicsNewsAdapter = new DbNewsAdapter(getActivity());
        businessNewsAdapter = new DbNewsAdapter(getActivity());
        politics_recyclerView.setAdapter(politicsNewsAdapter);
        business_recyclerView.setAdapter(businessNewsAdapter);
        politics_recyclerView.setLayoutManager(linearLayoutManager);
        business_recyclerView.setLayoutManager(linearLayoutManagerbusiness);
        politics_recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        politics_recyclerView.setNestedScrollingEnabled(false);
        business_recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        business_recyclerView.setNestedScrollingEnabled(false);


        //politicsNewsAdapter.swapCursor(politicsCursor);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                NewsContract.Entry.CONTENT_URI,
                // Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                PROJECTION,
                null, null, NewsContract.Entry.COLUMN_NAME_PUBLISHED + " DESC" );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        if (data.getCount() != 0) {
            mErrorTextView.setVisibility(View.GONE);
        }
        mAdapter.setCursor(data);
        politicsCursor = getPoliticsNews();
        businessCursor = getBusinessNews();
        politicsNewsAdapter.swapCursor(politicsCursor);
        businessNewsAdapter.swapCursor(businessCursor);


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.setCursor(null);
       // politicsNewsAdapter.swapCursor(null);
    }


    public void onRefresh() {

        NewsSyncJobs.syncImmediately(getActivity());

        if (!networkUp() && mAdapter.getItemCount() == 0) {

            mErrorTextView.setText(getString(R.string.error_no_network));
            mErrorTextView.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {

            Toast.makeText(getActivity(), R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else {
            mErrorTextView.setVisibility(View.GONE);
        }

    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private Cursor getPoliticsNews() {
        return mDb.query(
                NewsContract.Entry.TABLE_NAME_POLITICS,
                null, // Column
                null, // Where clause
                null, // Arguments
                null, // Group by
                null, // having
                NewsContract.Entry.COLUMN_NAME_PUBLISHED + " DESC" // Sort_order
        );


    }

    private Cursor getBusinessNews() {
        return mDb.query(
                NewsContract.Entry.TABLE_NAME_BUSINESS,
                null, // Column
                null, // Where clause
                null, // Arguments
                null, // Group by
                null, // having
                NewsContract.Entry.COLUMN_NAME_PUBLISHED + " DESC" // Sort_order
        );


    }
}
