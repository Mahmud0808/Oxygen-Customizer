package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.findClassInArray;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class OpUtils extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    public static Class<?> QsColorUtil = null;
    private static Class<?> OpUtils = null;
    private static Class<?> QSFragmentHelper = null;

    public OpUtils(Context context) {
        super(context);
    }

    public static int getPrimaryColor(Context mContext) {
        if (mContext == null) return Color.WHITE;
        int colorIfNull = ResourcesCompat.getColor(mContext.getResources(), android.R.color.system_accent1_600, mContext.getTheme());
        if (OpUtils == null) return colorIfNull;
        int color;
        try {
            color = (int) callStaticMethod(OpUtils, "getThemeAccentColor", mContext, true);
        } catch (Throwable t) {
            color = colorIfNull;
        }
        return color;
    }

    public static boolean isMediaIconNeedUseLightColor(Context context) {
        if (QsColorUtil == null) return false;
        try {
            return (boolean) callStaticMethod(QsColorUtil, Build.VERSION.SDK_INT >= 35 ? "isIconNeedUseLightColor" : "isMediaIconNeedUseLightColor", context);
        } catch (Throwable t) {
            return false;
        }
    }

    public static int getIconLightColor() {
        if (QsColorUtil == null) return Color.WHITE;
        try {
            return getStaticIntField(QsColorUtil, "BRIGHTNESS_ICON_BG_LIGHT_COLOR");
        } catch (Throwable t) {
            return Color.WHITE;
        }
    }

    public static int getTileActiveColor(Context context) {
        if (QSFragmentHelper == null) return getPrimaryColor(context);
        try {
            Object QSFragmentHelperObj = callStaticMethod(QSFragmentHelper, "getInstance");
            return (int) callMethod(QSFragmentHelperObj, "getActiveColorWithDarkMode", context);
        } catch (Throwable t) {
            return getPrimaryColor(context);
        }
    }

    @Override
    public void updatePrefs(String... Key) {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        try {
            OpUtils = findClass("com.oplusos.systemui.util.OpUtils", lpparam.classLoader);
        } catch (Throwable t) {
            OpUtils = null;
        }

        QsColorUtil = findClassInArray(lpparam,
                "com.oplus.systemui.qs.base.util.QsColorUtil" /* OOS15 */,
                "com.oplus.systemui.qs.util.QsColorUtil" /* OOS13-14 */);

        try {
            QSFragmentHelper = findClass("com.oplus.systemui.qs.helper.QSFragmentHelper", lpparam.classLoader);
        } catch (Throwable ignored) {
            QSFragmentHelper = findClass("com.oplusos.systemui.qs.helper.QSFragmentHelper", lpparam.classLoader);
        }

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
