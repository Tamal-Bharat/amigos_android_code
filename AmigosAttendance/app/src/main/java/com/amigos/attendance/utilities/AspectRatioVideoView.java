package com.amigos.attendance.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class AspectRatioVideoView extends VideoView {

    private int videoWidth;
    private int videoHeight;

    public AspectRatioVideoView(Context context) {
        super(context);
    }

    public AspectRatioVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioVideoView(Context context,
                                AttributeSet attrs,
                                int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setVideoDimensions(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (videoWidth > 0 && videoHeight > 0) {
            //int height = (int) (width * ((float) videoHeight / videoWidth));
            int height = (int) (400 * getResources().getDisplayMetrics().density);
            setMeasuredDimension(width, height);
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }
}
