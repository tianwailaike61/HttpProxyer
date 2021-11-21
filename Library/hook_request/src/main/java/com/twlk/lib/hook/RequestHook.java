package com.twlk.lib.hook;

import android.content.Context;


import okhttp3.OkHttpClient;

/**
 * @author hongkui.jiang
 * @Date 2020/6/30
 */
public class RequestHook {

    private static HookChain chain;
    private static boolean IS_INSTALL = false;

    public static void init(Context context) {
        if (IS_INSTALL) {
            return;
        }
        //TODO 安装DEX
        chain = new HookChain();
        IS_INSTALL = true;
    }

    public static IClientInterceptorAdd getChain() {
        return chain;
    }

    public static void updateOkhttpClientBuilder(Object builder) {
        setInterceptors(builder);
    }

    public static void updateOkhttpClientBuilder(Object builder, Object okHttpClient) {
        setInterceptors(builder);
    }

    private static void setInterceptors(Object builder) {
        if (!(builder instanceof OkHttpClient.Builder)) {
            return;
        }

        OkHttpClient.Builder localBuilder = (OkHttpClient.Builder) builder;
        chain.intercept(localBuilder);
    }
}