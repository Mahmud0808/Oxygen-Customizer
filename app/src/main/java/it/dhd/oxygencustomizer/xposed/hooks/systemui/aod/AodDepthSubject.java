package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_AOD_INVALIDATE_DEPTH;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.io.FileInputStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class AodDepthSubject extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;

    private boolean mDepthWallpaper = false;
    private int mDepthSubjectAlpha = 0;

    private FrameLayout mAodRootLayout = null;
    private FrameLayout mLockScreenSubject;
    private boolean mLayersCreated = false;
    private boolean mSubjectCacheValid = false;
    private boolean mReceiverRegistered = false;

    private BroadcastReceiver mInvalidateCacheReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSubjectCacheValid = false;
            loadDepth();
        }
    };

    public AodDepthSubject(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mDepthWallpaper = Xprefs.getBoolean("DWShowOnAod", false);
        mDepthSubjectAlpha = Xprefs.getSliderInt("DWAodOpacity", 192);

        if (mLayersCreated) {
            mLockScreenSubject.setVisibility(mDepthWallpaper ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!mReceiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTIONS_AOD_INVALIDATE_DEPTH);
            mContext.registerReceiver(mInvalidateCacheReceiver, filter, Context.RECEIVER_EXPORTED);
            mReceiverRegistered = true;
        }

        Class<?> AodRootLayout;
        try {
            AodRootLayout = findClass("com.oplus.systemui.aod.aodclock.off.AodRootLayout", lpparam.classLoader);
        } catch (Throwable t) {
            AodRootLayout = findClass("com.oplusos.systemui.aod.aodclock.off.AodRootLayout", lpparam.classLoader);
        }
        hookAllConstructors(AodRootLayout, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mAodRootLayout = (FrameLayout) param.thisObject;
                mAodRootLayout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if (!mDepthWallpaper) return;
                    else {
                        if (mLockScreenSubject.getVisibility() != View.VISIBLE) {
                            mLockScreenSubject.setVisibility(View.VISIBLE);
                        }
                    }
                    if (v.getVisibility() == View.VISIBLE) {
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator());
                        fadeIn.setDuration(1000);
                        mLockScreenSubject.startAnimation(fadeIn);
                    } else {
                        Animation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setInterpolator(new DecelerateInterpolator());
                        fadeOut.setDuration(1000);
                        mLockScreenSubject.startAnimation(fadeOut);
                    }
                });
                placeSubject();
            }
        });

    }

    private void placeSubject() {
        if (mAodRootLayout == null) {
            return;
        }
        createLayers();
        if (!mSubjectCacheValid) loadDepth();
        try {
            ViewGroup v = (ViewGroup) mLockScreenSubject.getParent();
            v.removeView(mLockScreenSubject);
        } catch (Throwable t) {
            log(t);
        }
        mAodRootLayout.addView(mLockScreenSubject, 0);
    }

    private void createLayers() {
        if (mLayersCreated) return;
        mLockScreenSubject = new FrameLayout(mContext);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        mLockScreenSubject.setLayoutParams(lp);
        mLayersCreated = true;
    }

    private void loadDepth() {
        try (FileInputStream inputStream = new FileInputStream(Constants.getLockScreenSubjectCachePath())) {
            log("Loading Depth Cache");
            Drawable bitmapDrawable = BitmapDrawable.createFromStream(inputStream, "");
            bitmapDrawable.setAlpha(255);

            mLockScreenSubject.setBackground(bitmapDrawable);
            mLockScreenSubject.getBackground().setAlpha(mDepthSubjectAlpha);
            mSubjectCacheValid = true;
        } catch (Throwable t) {
            log(t);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
