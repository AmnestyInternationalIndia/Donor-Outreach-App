package in.org.amnesty.outreach.fragments;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.adapters.RecyclerImageAdapter;
import in.org.amnesty.outreach.adapters.RecyclerPdfAdapter;
import in.org.amnesty.outreach.adapters.RecyclerVideoAdapter;
import in.org.amnesty.outreach.helpers.Utils;
import in.org.amnesty.outreach.views.RecyclerPlusView;

public class BaseTabFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final int TYPE_VIDEO = 0;

    public static final int TYPE_IMAGE = 1;

    public static final int TYPE_PDF = 2;

    public static final String BUNDLE_CURRENT_TYPE = "currentType";

    private static final int LOADER_ID_VIDEO = 1001;

    private static final int LOADER_ID_IMAGE = 1002;

    private static final String PDF_EXTENSION = ".pdf";

    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<File> mPdfFileList;

    private int mCurrentType;

    private RelativeLayout mProgressLayout;

    private RelativeLayout mContentLayout;

    public BaseTabFragment() {
        // Required empty public constructor
    }

    public static BaseTabFragment newInstance(Bundle arguments) {
        BaseTabFragment fragment = new BaseTabFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
            switch (mCurrentType) {
                case TYPE_VIDEO:
                    initLoader(true, LOADER_ID_VIDEO);
                    break;
                case TYPE_IMAGE:
                    initLoader(true, LOADER_ID_IMAGE);
                    break;
                case TYPE_PDF:
                    new FileLoadTask().execute();
                    break;
                default:

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View baseTabView = inflater.inflate(R.layout.fragment_base_tab, null);

        Bundle arguments = getArguments();

        if (arguments != null) {
            mCurrentType = arguments.getInt(BUNDLE_CURRENT_TYPE, TYPE_VIDEO);
        }

        mContentLayout = (RelativeLayout) baseTabView.findViewById(R.id.contentLayout);
        RelativeLayout mNoContentLayout = (RelativeLayout) baseTabView.findViewById(R.id.noContentLayout);
        mProgressLayout = (RelativeLayout) baseTabView.findViewById(R.id.progressLayout);

        GridLayoutManager mLayoutManager = new GridLayoutManager(getParentActivity(),
                getParentActivity().getResources().getInteger(R.integer.recycler_column_count));
        mLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);

        RecyclerPlusView mRecyclerView = (RecyclerPlusView) baseTabView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setEmptyView(mNoContentLayout);

        switch (mCurrentType) {
            case TYPE_VIDEO:
                mRecyclerAdapter = new RecyclerVideoAdapter(getParentActivity(), null);
                initLoader(false, LOADER_ID_VIDEO);
                break;
            case TYPE_IMAGE:
                mRecyclerAdapter = new RecyclerImageAdapter(getParentActivity(), null);
                initLoader(false, LOADER_ID_IMAGE);
                break;
            case TYPE_PDF:
                mRecyclerAdapter = new RecyclerPdfAdapter(getParentActivity(), mPdfFileList);
                break;
            default:
        }
        mRecyclerView.setAdapter(mRecyclerAdapter);

        return baseTabView;
    }

    public void loadPdfFiles(File directory) {

        final File[] files = directory.listFiles();
        mPdfFileList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(PDF_EXTENSION)) {
                    mPdfFileList.add(file);
                }
            }
        }
    }

    private void initLoader(boolean mReset, int loaderId) {
        if (mReset) {
            getLoaderManager().restartLoader(loaderId, null, this);
            return;
        }
        getLoaderManager().initLoader(loaderId, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MediaStore.MediaColumns.DATA + " like ?";
        String[] selectionArgs = new String[]{"%" + Utils.Constants.DEFAULT_APP_FOLDER + "%"};
        String[] projection = {MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE};
        switch (mCurrentType) {
            case TYPE_IMAGE:
                return new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection, selection, selectionArgs,
                        MediaStore.MediaColumns.DATE_ADDED + " DESC");
            default:
                return new CursorLoader(getActivity(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projection, selection, selectionArgs,
                        MediaStore.MediaColumns.DATE_ADDED + " DESC");
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!Utils.isCursorEmpty(data)) {
            switch (mCurrentType) {
                case TYPE_IMAGE:
                    ((RecyclerImageAdapter) mRecyclerAdapter).swapCursor(data);
                    break;
                default:
                    ((RecyclerVideoAdapter) mRecyclerAdapter).swapCursor(data);
            }

            mRecyclerAdapter.notifyDataSetChanged();
        }

        mProgressLayout.setVisibility(View.GONE);
        mContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private class FileLoadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            loadPdfFiles(Utils.DEFAULT_APP_PATH);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((RecyclerPdfAdapter) mRecyclerAdapter).swapDataSet(mPdfFileList);
            mProgressLayout.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean getUserVisibleHint() {
        if(!isMenuVisible()) {
            switch (mCurrentType) {
                case TYPE_VIDEO:
                    ((RecyclerVideoAdapter) mRecyclerAdapter).swapCursor(null);
                    break;
                case TYPE_IMAGE:
                    ((RecyclerImageAdapter) mRecyclerAdapter).swapCursor(null);
                    break;
                case TYPE_PDF:
                    ((RecyclerPdfAdapter) mRecyclerAdapter).swapDataSet(null);
                    break;
                default:
            }
        }

        return isMenuVisible();
    }

    @Override
    public void onPause() {
        super.onPause();
        switch (mCurrentType) {
            case TYPE_VIDEO:
                ((RecyclerVideoAdapter) mRecyclerAdapter).swapCursor(null);
                break;
            case TYPE_IMAGE:
                ((RecyclerImageAdapter) mRecyclerAdapter).swapCursor(null);
                break;
            case TYPE_PDF:
                ((RecyclerPdfAdapter) mRecyclerAdapter).swapDataSet(null);
                break;
            default:
        }
    }
}
