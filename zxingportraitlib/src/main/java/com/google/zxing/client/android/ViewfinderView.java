/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import com.cai.zxingportraitlib.R;
import com.google.zxing.client.android.camera.CameraManager;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
    public static int VIEW_HEIGHT;

    private static final int POINT_SIZE = 6;

    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    Bitmap bitmap_lb;
    Bitmap bitmap_rb;
    Bitmap bitmap_lt;
    Bitmap bitmap_rt;
    Bitmap bitmap_laser;

    private float factor;
    ObjectAnimator animator;
    String tip;
    float txt_width;
    float txt_size;
    int txtColor;
    int txtMargin;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every
        // time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskColor = Color.parseColor("#BF000000");
        txtColor = Color.parseColor("#BFFFFFFF");

        bitmap_lb = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_zx_lb)).getBitmap();
        bitmap_rb = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_zx_rb)).getBitmap();
        bitmap_lt = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_zx_lt)).getBitmap();
        bitmap_rt = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_zx_rt)).getBitmap();
        bitmap_laser = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_zx_laser)).getBitmap();
        tip = getResources().getText(R.string.msg_default_status).toString();
        txt_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics());
        paint.setTextSize(txt_size);
        txt_width = paint.measureText(tip);
        txtMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30,
                getResources().getDisplayMetrics());

        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 0.99f);
        // animator = ObjectAnimator.ofFloat(this, "factor", 0.0f, 0.99f);
        animator.setDuration(3500).setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                factor = (Float) animation.getAnimatedValue();
                if (frame != null) {
                    postInvalidate(frame.left - POINT_SIZE, frame.top - POINT_SIZE, frame.right + POINT_SIZE,
                            frame.bottom + POINT_SIZE);
                }
            }
        });

        animator.start();
    }

    public void setFactor(float f) {
        this.factor = f;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        VIEW_HEIGHT = getMeasuredHeight();
    }

    Rect frame;
    Rect rect_laser = new Rect();

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        canvas.drawBitmap(bitmap_lt, frame.left, frame.top, null);
        canvas.drawBitmap(bitmap_rt, frame.right - bitmap_rt.getWidth() + 1, frame.top, null);
        canvas.drawBitmap(bitmap_lb, frame.left, frame.bottom - bitmap_lb.getHeight() + 1, null);
        canvas.drawBitmap(bitmap_rb, frame.right - bitmap_rb.getWidth() + 1, frame.bottom - bitmap_rb.getHeight() + 1,
                null);

        // canvas.drawBitmap(bitmap_laser, frame.left, (frame.top +
        // (frame.bottom - frame.top) * factor), null);
        rect_laser.left = frame.left;
        rect_laser.right = frame.right;
        rect_laser.top = (int) (frame.top + (frame.bottom - frame.top) * factor);
        rect_laser.bottom = bitmap_laser.getHeight() + rect_laser.top;
        canvas.drawBitmap(bitmap_laser, null, rect_laser, null);

        paint.setTextSize(txt_size);
        paint.setColor(txtColor);
        canvas.drawText(tip, getWidth() / 2 - txt_width / 2, frame.bottom + txtMargin, paint);
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

}
