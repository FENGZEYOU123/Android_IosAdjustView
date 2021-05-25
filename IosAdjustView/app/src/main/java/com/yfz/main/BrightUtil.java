package com.yfz.main;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.view.WindowManager;

/**
 * 作者：YFZ
 * 简介：调节屏幕亮度工具类
 * Android技术生活-QQ交流群:723592501
 */
public class BrightUtil {

    /**
     * 获取当前系统亮度
     * 返回-1视为获取失败
     * @return
     */
    public static int getSystemBrightness(Context context){
        if(null != context) {
            try {
                return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
    /**
     * 改变当前系统亮度-最大亮度为255 -注意：当为自动亮度调节模式下，手动设置无效，需将自动亮度调节先关闭
     * @return
     */
    public static void setSystemBrightness(Context context, int brightness) {
        if(null != context) {
            if(brightness>=255){
                brightness=255;
            }
            if(brightness<=0){
                brightness=0;
            }
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,brightness);
        }
    }
    /**
     * 改变当前app屏幕亮度-最大亮度为255 -注意：当为自动亮度调节模式下，手动设置无效，需将自动亮度调节先关闭
     * 不会影响到手机系统整体的亮度
     * @return
     */
    public static void setAppBrightness(Activity activity, int brightness) {
        if(null != activity) {
            if(brightness>=255){
                brightness=255;
            }
            if(brightness<=0){
                brightness=0;
            }
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
            activity.getWindow().setAttributes(lp);
        }
    }
    /**
     * 关闭自动亮度调节
     */
    public static void stopAutoBrightness(Context context) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }
    /**
     * 获取是否为自动亮度调节模式
     */
    public static boolean getIsAutoBrightnessMode(Context context){
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 获取是否为手动亮度调节模式
     */
    public static boolean getIsManualBrightnessMode(Context context){
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 监听亮度的改变
     */
    public static class BrightnessObserver extends ContentObserver {
        private Context mContext;
        private OnBrightnessChangeListener mOnBrightnessChangeListener;
        //创建实例所需参数
        public BrightnessObserver(Context context, Handler handler, OnBrightnessChangeListener onBrightnessChangeListener) {
            super(handler);
            this.mContext = context;
            this.mOnBrightnessChangeListener = onBrightnessChangeListener;
            register();
        }
        //注册监听
        public void register(){
            if(mContext != null) {
                Uri brightnessUri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
                mContext.getContentResolver().registerContentObserver(brightnessUri, true, this);
            }
        }
        //取消注册监听
        public void unregister(){
            if(mContext != null) {
                mContext.getContentResolver().unregisterContentObserver(this);
            }
        }
        //通过接口返回当前亮度
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if(null != mOnBrightnessChangeListener && null != mContext){  //通过接口返回当前亮度
                mOnBrightnessChangeListener.onChange(getSystemBrightness(mContext));
            }
        }
    }

    /**
     * 监听亮度改变-接口
     */
    public interface OnBrightnessChangeListener {
        void onChange(int brightness);
    }


}
