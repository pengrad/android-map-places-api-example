package com.github.pengrad.mapsplaces;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Stas Parshin
 * 01 December 2015
 */
public class SliderListener extends SlidingUpPanelLayout.SimplePanelSlideListener {

    private Toolbar toolbar, slideToolbar;
    private View slideTitle;
    private RecyclerView recyclerView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private int titleColor;

    public SliderListener(Toolbar toolbar, Toolbar slideToolbar, View slideTitle, RecyclerView recyclerView,
                          SlidingUpPanelLayout slidingUpPanelLayout, int titleColor) {
        this.toolbar = toolbar;
        this.slideToolbar = slideToolbar;
        this.slideTitle = slideTitle;
        this.recyclerView = recyclerView;
        this.slidingUpPanelLayout = slidingUpPanelLayout;
        this.titleColor = titleColor;
    }

    @Override
    public void onPanelExpanded(View panel) {
        toolbar.setVisibility(View.INVISIBLE);
        slideTitle.setVisibility(View.GONE);
        slideToolbar.setVisibility(View.VISIBLE);
        slideToolbar.setTitle("Results");
        slideToolbar.setTitleTextColor(titleColor);
        slideToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        slideToolbar.setNavigationOnClickListener(v -> slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onPanelCollapsed(View panel) {
        toolbar.setVisibility(View.VISIBLE);
        slideToolbar.setVisibility(View.GONE);
        slideTitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPanelAnchored(View panel) {
        toolbar.setVisibility(View.VISIBLE);
        slideToolbar.setVisibility(View.GONE);
        slideTitle.setVisibility(View.VISIBLE);
        int height = slidingUpPanelLayout.getHeight() / 2 - slidingUpPanelLayout.getPanelHeight() / 2;
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

}
