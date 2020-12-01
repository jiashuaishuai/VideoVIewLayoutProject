package com.jiashuai.videoviewlayoutproject.videoview;

import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * 视频圆角
 */
public class TextureVideoViewOutlineProvider extends ViewOutlineProvider {
    private float mRadius;

    public TextureVideoViewOutlineProvider(float radius) {
        this.mRadius = radius;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        int left = view.getLeft();
        int width = view.getWidth();
        int height = view.getHeight();
        int top = view.getTop();
        int leftMargin = 0;
        int topMargin = 0;
        Rect selfRect = new Rect(leftMargin, topMargin, width, height);
        outline.setRoundRect(selfRect, mRadius);
    }
}