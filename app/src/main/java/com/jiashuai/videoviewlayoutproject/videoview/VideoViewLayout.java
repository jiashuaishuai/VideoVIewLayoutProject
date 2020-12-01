package com.jiashuai.videoviewlayoutproject.videoview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.jiashuai.videoviewlayoutproject.App;
import com.jiashuai.videoviewlayoutproject.MainActivity;
import com.jiashuai.videoviewlayoutproject.R;


public class VideoViewLayout extends ConstraintLayout {

    boolean isShowProgress = true;
    long mLastTime = 0;
    long mCurTime = 0;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    isShowProgress = !isShowProgress;
                    startAnim();
                    break;
                case 2:
                    videoController.doPauseResume();
                    break;
            }
        }
    };
    private final Context mContext;
    private String videoPath;
    TextureVideoView videoView;
    VideoController videoController;
    ImageView btnPlay;
    ImageView img_cover;
    private int bottomMargin;


    public VideoViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public VideoViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.view_video_view_layout, this);
        videoView = findViewById(R.id.video_view);
        videoController = findViewById(R.id.video_view_controller);
        btnPlay = findViewById(R.id.btn_icon_play);
        img_cover = findViewById(R.id.img_cover);
        videoController.setMediaPlayer(videoView);
//        videoController.setFullScreen(true);

        videoView.setOutlineProvider(new TextureVideoViewOutlineProvider(10));
        videoView.setClipToOutline(true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                videoView.seekTo(0);
                img_cover.setVisibility(GONE);
                videoController.start();
            }
        });
        videoController.setVideoChangerListener(new VideoController.VideoChangerListener() {
            @Override
            public void start() {
                btnPlay.setVisibility(GONE);
            }

            @Override
            public void pause() {
                btnPlay.setVisibility(VISIBLE);
            }
        });
        btnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                videoController.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setVisibility(VISIBLE);
                videoController.completion();
                videoController.setCurrentPosition(0);
//                videoController.start();
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLastTime = mCurTime;
                mCurTime = System.currentTimeMillis();
                if (mCurTime - mLastTime < 300) {//双击事件
                    mCurTime = 0;
                    mLastTime = 0;
                    handler.removeMessages(1);
                    handler.sendEmptyMessage(2);
                } else {//单击事件
                    handler.sendEmptyMessageDelayed(1, 310);
                }
            }
        });
        LayoutParams layoutParams = (LayoutParams) videoController.getLayoutParams();
        bottomMargin = layoutParams.bottomMargin;
        videoController.setFullScreenClick(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoController.isFullScreen()) {
//                    if (App.getInstance().isPad()) {
//                        VideoDialog videoDialog;
//                        videoDialog = new VideoDialog(mContext);
//                        videoDialog.show();
//                        videoDialog.setVideoPath(videoPath);
//                    } else {
//                        ((MainActivity) mContext).start(PreviewVideoFragment.newInstance(videoPath));
//                    }
                }
            }
        });

    }

    private void startAnim() {
        if (isShowProgress) {
            ObjectAnimator translationY = ObjectAnimator.ofFloat(videoController, "translationY", videoController.getHeight() + bottomMargin, 0);
            translationY.setDuration(300);
            translationY.start();
        } else {
            ObjectAnimator translationY = ObjectAnimator.ofFloat(videoController, "translationY", 0, videoController.getHeight() + bottomMargin);
            translationY.setDuration(300);
            translationY.start();
        }
        if (animListener != null) {
            animListener.animChange(isShowProgress);
        }
    }

    VideoViewLayoutAnimListener animListener;

    public void setAnimListener(VideoViewLayoutAnimListener animListener) {
        this.animListener = animListener;
    }


    public interface VideoViewLayoutAnimListener {
        void animChange(boolean isShowProgress);
    }

    public void setFullScreenClick(OnClickListener click) {
        videoController.setFullScreenClick(click);
    }

    public void setFullScreen(boolean fullScreen) {
        videoController.setFullScreen(fullScreen);
    }


    /**
     * View 在当前页面显示隐藏时回调
     *
     * @param changedView
     * @param visibility
     */
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            if (videoController != null) {
                videoController.start();
            }
        } else if (visibility == View.GONE) {
            if (videoController != null) {
                videoController.pause();
            }
        }
    }


    /**
     * 设置videoPath
     *
     * @param path
     */
    public void setVideoPath(String path) {
        this.videoPath = path;
        videoView.setVideoPath(App.getProxy(mContext).getProxyUrl(videoPath));
    }

    /**
     * 设置封面
     *
     * @param coverPath
     */
    public void setCover(String coverPath) {
//        GlideApp.with(this).load(coverPath).loadRound(6).into(img_cover);
    }

    int currentPosition;

    public int getCurrentPosition() {
        return videoController.getCurrentPosition();
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        videoController.setCurrentPosition(currentPosition);
    }

    @Override
    protected void onDetachedFromWindow() {
        currentPosition = videoController.getCurrentPosition();
        super.onDetachedFromWindow();
    }
}
