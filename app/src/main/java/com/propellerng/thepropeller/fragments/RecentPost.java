package com.propellerng.thepropeller.fragments;


import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.propellerng.thepropeller.AppController;
import com.propellerng.thepropeller.adapter.NewsAdapter;
import com.propellerng.thepropeller.R;
import com.propellerng.thepropeller.database.NewsContract;
import com.propellerng.thepropeller.sync.NewsSyncJobs;
import com.propellerng.thepropeller.util.DividerItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentPost extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor>{


    public RecentPost() {
        // Required empty public constructor
    }

    private static final int STOCK_LOADER = 0;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipRefresh;
    private TextView mErrorTextView;
    private NewsAdapter mAdapter;
    private LinearLayoutManager linearLayoutManager;
    String[] PROJECTION = {
            NewsContract.Entry._ID,
            NewsContract.Entry.COLUMN_NAME_TITLE,
            NewsContract.Entry.COLUMN_NAME_PUBLISHED,
            NewsContract.Entry.COLUMN_NAME_LINK,
            NewsContract.Entry.COLUMN_NAME_DESCRIPTION,
            NewsContract.Entry.COLUMN_NAME_IMAGE_URL,
            NewsContract.Entry.COLUMN_NAME_FAV,

    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent_post, container, false);

        ((AppController) getActivity().getApplication()).setBASE_URL("http://propellerng.com/?json=get_posts&page=");
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        mSwipRefresh = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        mErrorTextView = (TextView)view.findViewById(R.id.error);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new NewsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mSwipRefresh.setOnRefreshListener(this);
        mSwipRefresh.setRefreshing(true);
        onRefresh();
        NewsSyncJobs.initialize(getActivity());

        getLoaderManager().initLoader(STOCK_LOADER, null, this);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                NewsContract.Entry.CONTENT_URI,
                // Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                PROJECTION,
                null, null, NewsContract.Entry.COLUMN_NAME_PUBLISHED);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mSwipRefresh.setRefreshing(false);

        if (data.getCount() != 0) {
            mErrorTextView.setVisibility(View.GONE);
        }
        mAdapter.setCursor(data);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSwipRefresh.setRefreshing(false);
        mAdapter.setCursor(null);
    }

    @Override
    public void onRefresh() {

        NewsSyncJobs.syncImmediately(getActivity());

        if (!networkUp() && mAdapter.getItemCount() == 0) {
            mSwipRefresh.setRefreshing(false);
            mErrorTextView.setText(getString(R.string.error_no_network));
            mErrorTextView.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            mSwipRefresh.setRefreshing(false);
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
}
