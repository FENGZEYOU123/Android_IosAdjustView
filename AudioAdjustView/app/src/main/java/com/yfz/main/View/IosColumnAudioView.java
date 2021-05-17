package com.yfz.main.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.rtp.AudioStream;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

import com.yfz.main.R;

/**
 * 作者：游丰泽
 * 简介：仿ios风格的音量控制view
 * CSDN: https://blog.csdn.net/ruiruiddd
 * GITHUB: https://github.com/FENGZEYOU123
 */
public class IosColumnAudioView extends View {
    private Context mContext;
    //防止手指持续移动最大距离
    private final double PREVENT_MOVE_DISTANCE = 2.0;
    //手指移动后,UI位移幅度大小
    private double mMoveDistance =10.0;
    //当前UI高度与view高度的比例
    private double mCurrentLoudRate = 0.5;
    //系统最大声音index
    private int mMaxLoud=0;
    //View背景
    private Drawable mDrawable_outside = null;
    //UI背景
    private Drawable mDrawable_inside = null;
    //记录按压时手指相对于组件view的高度
    private float mDownY;
    //系统audio管理
    private AudioManager mAM;
    //声音流类型int
    private int mAudioStreamInt= AudioManager.STREAM_MUSIC;
    //枚举-可选择调整的声音流类型
    public enum AudioStreamType{
        Music,
        System,
        VideoCall,
        Ring,
        Alarm,
        Notification,
        Bluetooth_Sco,
        SystemEnforced,
        DTMF,
        TTS,
        Assistant,
        Accessibility,
    }


    public IosColumnAudioView(Context context) {
        super(context);
        initial(context);
    }

    public IosColumnAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.IosColumnAudioView);
        mDrawable_outside=typedArray.getDrawable(R.styleable.IosColumnAudioView_IosColumnAudioView_setBackgroundOutSide);
        mDrawable_inside=typedArray.getDrawable(R.styleable.IosColumnAudioView_IosColumnAudioView_setBackgroundInSide);
        initial(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMoveDistance = MeasureSpec.getSize(heightMeasureSpec) * 0.05;
        if(null != mContext) {
            mMoveDistance = px2dip(mContext, mMoveDistance);
        }
    }

    private void initial(Context context){
        mContext=context;
        mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxLoud = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurrentLoudRate = (double) mAM.getStreamVolume(AudioManager.STREAM_MUSIC) / mMaxLoud;

        if(null == mDrawable_outside){
            setBackgroundColor(Color.GRAY);
        }else {
            setBackground(mDrawable_outside);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownY=event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mDownY - event.getY() > PREVENT_MOVE_DISTANCE){ //向上移动
                    mCurrentLoudRate = ( getHeight() * mCurrentLoudRate + mMoveDistance) /  getHeight();
                }else if(mDownY - event.getY() < -1 * PREVENT_MOVE_DISTANCE) {
                    mCurrentLoudRate = ( getHeight() * mCurrentLoudRate - mMoveDistance) /  getHeight();
                }
                if(mCurrentLoudRate >=1){
                    mCurrentLoudRate =1;
                }
                if(mCurrentLoudRate <=0){
                    mCurrentLoudRate =0;
                }
                mDownY=event.getY();
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        refresh();
        return true;
    }

    private void refresh(){
        mAM.setStreamVolume(AudioManager.STREAM_SYSTEM, (int)(mCurrentLoudRate * mMaxLoud), 0);
        Log.d("TAG", "refresh: "+(int)(mCurrentLoudRate * mMaxLoud));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(null != mDrawable_inside) {
            mDrawable_inside.setBounds(0,(getHeight()-(int)(getHeight() * mCurrentLoudRate)),getWidth(),getHeight());
            mDrawable_inside.draw(canvas);
        }
    }

    private double px2dip(Context context, double pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxValue / scale + 0.5f);
    }
//
//    private int getAudioStreamType(String streamType){
//        switch (streamType){
//            default:
//                break;
//        }
//    }
}
