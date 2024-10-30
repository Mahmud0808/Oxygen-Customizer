package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

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
    private Drawable mSubjectDimmingOverlay;

    public AodDepthSubject(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mDepthWallpaper = Xprefs.getBoolean("aod_depth_wallpaper", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Class<?> CentralSurfacesImpl = findClass("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);

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
                mAodRootLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
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
                    }
                });
                placeSubject();
            }
        });

//        // Stole Dozing State
//        hookAllMethods(CentralSurfacesImpl, "updateDozingState", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                boolean mDozing = getBooleanField(param.thisObject, "mDozing");
//                mLockScreenSubject.setVisibility(mDozing ? View.VISIBLE : View.GONE);
//            }
//        });
//
//
//        hookAllMethods(CentralSurfacesImpl, "inflateStatusBarWindow", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                mAodRootLayout = (FrameLayout) getObjectField(param.thisObject, "mNotificationShadeWindowView");
//                placeSubject();
//            }
//        });

    }

    private void placeSubject() {
        if (mAodRootLayout == null) {
            return;
        }
        createLayers();
        loadDepth();
        try {
            ViewGroup v = (ViewGroup) mLockScreenSubject.getParent();
            v.removeView(mLockScreenSubject);
        } catch (Throwable t) {
            log(t);
        }
        mAodRootLayout.addView(mLockScreenSubject, 0);
    }

    private void createLayers() {
        mLockScreenSubject = new FrameLayout(mContext);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        mLockScreenSubject.setLayoutParams(lp);
    }

    private void loadDepth() {
        try (FileInputStream inputStream = new FileInputStream(Constants.getLockScreenSubjectCachePath())) {
            log("Loading Lock Screen Subject Cache file exists");
            Drawable bitmapDrawable = BitmapDrawable.createFromStream(inputStream, "");
            bitmapDrawable.setAlpha(255);

            mSubjectDimmingOverlay = bitmapDrawable.getConstantState().newDrawable().mutate();
            mSubjectDimmingOverlay.setTint(Color.BLACK);

            mLockScreenSubject.setBackground(bitmapDrawable);
            mLockScreenSubject.getBackground().setAlpha(192);
//            mSubjectDimmingOverlay.setAlpha(255 /*Math.round(192 * (DWOpacity / 255f))*/);
        } catch (Throwable t) {
            log(t);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
