package com.jiashuai.videoviewlayoutproject.videoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.jiashuai.videoviewlayoutproject.R;

import java.util.Formatter;
import java.util.Locale;


public class VideoController extends LinearLayout {
    private MediaController.MediaPlayerControl mPlayer;
    private TextView mEndTime;
    private TextView mCurrentTime;
    private ImageView mPlaySwitch;
    private ImageView mFullScreenSwitch;
    private SeekBar mProgress;
    private Context mContext;
    VideoChangerListener videoChangerListener;
    OnClickListener fullScreenClick;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;//格式化
    private boolean mDragging;
    private boolean isFullScreen;
    private boolean isShowFullScreen = true;

    public VideoController(@NonNull Context context) {
        this(context, null);
    }

    public VideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.view_texture_video_controller, this);
        mPlaySwitch = findViewById(R.id.btn_play);
        mCurrentTime = findViewById(R.id.tv_current_time);

        mProgress = findViewById(R.id.progress);

        mEndTime = findViewById(R.id.tv_total_time);

        mFullScreenSwitch = findViewById(R.id.btn_full_screen);

        mPlaySwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doPauseResume();
            }
        });

        mProgress.setMax(1000);
        mProgress.setOnSeekBarChangeListener(mSeekListener);

        mProgress.setSplitTrack(false);

        mFullScreenSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullScreenClick != null) {
                    pause();
                    fullScreenClick.onClick(v);
                }
            }
        });


        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        if (mEndTime != null)
            mEndTime.setText(stringForTime(0));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(0));

    }

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }


    public void start() {
        isFirstStartCall = true;
        removeCallbacks(mShowProgress);
        mPlayer.start();
        updatePausePlay();
        post(mShowProgress);

    }

    public void pause() {
        mPlayer.pause();
        if (videoChangerListener != null) {
            videoChangerListener.pause();
        }
        updatePausePlay();
        removeCallbacks(mShowProgress);
    }

    public void completion() {
        removeCallbacks(mShowProgress);
        updatePausePlay();
        mProgress.setProgress(0);
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(0));
        mPlayer.seekTo(0);
    }

    public void doPauseResume() {
        if (mPlayer.isPlaying()) {
            pause();
        } else {
            start();
        }
    }


    private void updatePausePlay() {
        mPlaySwitch.setSelected(mPlayer.isPlaying());
        mFullScreenSwitch.setSelected(isFullScreen);
        mFullScreenSwitch.setVisibility(isShowFullScreen ? VISIBLE : GONE);
    }

    private boolean isFirstStartCall = true;

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mPlayer.isPlaying()) {
                post(mShowProgress);
            }
            if (pos > 150 && isFirstStartCall) {
                if (videoChangerListener != null) {
                    isFirstStartCall = false;
                    videoChangerListener.start();
                }
            }
        }
    };

    private int currentPosition;

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        mPlayer.seekTo(currentPosition);
    }

    private int setProgress() {
        if (mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                currentPosition = position;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));
        return position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = Math.round(timeMs / 1000f);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newposition));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mDragging = true;
            mPlayer.pause();
            removeCallbacks(mShowProgress);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
            mPlayer.start();
            updatePausePlay();
            setProgress();
            post(mShowProgress);
        }
    };

    private int dpi2px(int value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5);
    }

    /**
     * 全屏，全屏样式和窗口样式不一样
     *
     * @param fullScreen
     */
    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
        updatePausePlay();
    }

    public void setFullScreenClick(OnClickListener fullScreenClick) {
        this.fullScreenClick = fullScreenClick;
    }

    public void setVideoChangerListener(VideoChangerListener videoChangerListener) {
        this.videoChangerListener = videoChangerListener;
    }

    public interface VideoChangerListener {
        void start();

        void pause();
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setShowFullScreen(boolean showFullScreen) {
        isShowFullScreen = showFullScreen;
    }
}
