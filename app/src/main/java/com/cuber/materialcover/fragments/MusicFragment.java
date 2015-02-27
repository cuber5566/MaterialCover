package com.cuber.materialcover.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.cuber.materialcover.R;
import com.cuber.materialcover.adapters.CustomGridViewAdapter;
import com.cuber.materialcover.views.CustomGridView;

/**
 * Created by cuber on 2015/1/30.
 */
public class MusicFragment extends Fragment implements Handler.Callback {

    private static MusicFragment fragment;
    private Handler backgroundHandler;
    private Handler uiHandler;

    private CustomGridView gridView;
    private CustomGridViewAdapter imagesAdapter;
    private GridLayoutManager gridLayoutManager;
    private int itemSize;

    /**
     * New Instance
     */
    public static MusicFragment newInstance() {
        fragment = new MusicFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Life Cycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setupHandlers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music, container, false);
        initGridView(rootView);
        setupCustomGridViewSize();
        return rootView;
    }

    @Override
    public void onDestroy() {
        backgroundHandler.removeCallbacksAndMessages(null);
        uiHandler.removeCallbacksAndMessages(null);
        backgroundHandler.getLooper().quit();
        super.onDestroy();
    }

    private void initGridView(View rootView) {
        gridView = (CustomGridView) rootView.findViewById(R.id.grid_view);
        gridLayoutManager = new GridLayoutManager(rootView.getContext(), 2);
        gridView.setLayoutManager(gridLayoutManager);
        gridView.getItemAnimator().setSupportsChangeAnimations(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_settings) {
            imagesAdapter = new CustomGridViewAdapter(itemSize);
            gridView.setAdapter(imagesAdapter);
            gridView.scheduleLayoutAnimation();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setupCustomGridViewSize() {
        final ViewTreeObserver vto = gridView.getViewTreeObserver();
        if (vto != null) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                int lastWidth = -1;

                @Override
                public void onGlobalLayout() {
                    int width = gridView.getWidth() - gridView.getPaddingLeft() - gridView.getPaddingRight();
                    if (width == lastWidth || width <= 0) {
                        return;
                    }

                    // Compute number of columns
                    int maxItemWidth = gridView.getDefaultCellWidth();
                    int numColumns = 1;
                    while (true) {
                        if (width / numColumns > maxItemWidth) {
                            ++numColumns;
                        } else {
                            break;
                        }
                    }

                    itemSize = width / numColumns;
                    if (imagesAdapter != null) {
                        imagesAdapter.setItemSize(itemSize);
                    }
                    gridLayoutManager.setSpanCount(numColumns);

                }
            });
        }
    }

    /**
     * Setup Handler
     */
    private void setupHandlers() {
        HandlerThread handlerThread = new HandlerThread("MusicFragment.background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper(), this);
        uiHandler = new Handler(getActivity().getMainLooper(), this);
    }

    /**
     * Handle Message
     */
    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
