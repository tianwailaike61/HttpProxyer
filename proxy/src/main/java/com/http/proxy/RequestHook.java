/*
 * MIT License
 *
 * Copyright (c) 2020 tianwailaike61
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.http.proxy;

import android.content.Context;
import android.content.SharedPreferences;

import com.ding.library.CaptureInfoInterceptor;
import com.http.proxy.proxy.HookProxy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * @author hongkui.jiang
 * @Date 2020/6/30
 */
public class RequestHook {

    public static List<Interceptor> interceptors;
    private static boolean IS_INSTALL = false;
    public static HookProxy proxy;

    public static void init(Context context) {
        if (IS_INSTALL) {
            return;
        }
        if (interceptors == null) {
            interceptors = new ArrayList<>(1);
        }
        interceptors.add(new CaptureInfoInterceptor());
        SharedPreferences sp = context.getSharedPreferences("Proxy", Context.MODE_PRIVATE);
        if (sp.getBoolean("open", false)) {
            proxy = new HookProxy(sp.getString("ip", "10.200.242.98"), sp.getInt("port", 8282));
        } else {
            proxy = new HookProxy(HookProxy.Companion.getEMPTY());
        }
        IS_INSTALL = true;
    }

    public static void updateOkhttpClientBuilder(Object builder) {
        setInterceptors(builder);
    }

    public static void updateOkhttpClientBuilder(Object builder, Object okHttpClient) {
        setInterceptors(builder);
    }

    private static void setInterceptors(Object builder) {
        if (interceptors == null || interceptors.isEmpty()) {
            return;
        }
        if (!(builder instanceof OkHttpClient.Builder)) {
            return;
        }
        try {
            OkHttpClient.Builder localBuilder = (OkHttpClient.Builder) builder;
            if (proxy != null) {
                localBuilder.proxy(proxy);
            }

            List<Interceptor> lastInterceptorList = localBuilder.interceptors();
            if (lastInterceptorList.isEmpty()) {
                for (Interceptor interceptor : interceptors) {
                    localBuilder.addInterceptor(interceptor);
                }
                return;
            }
            List<Interceptor> interceptors = removeDuplicate(localBuilder.interceptors());
            Field field = OkHttpClient.Builder.class.getField("interceptors");
            field.setAccessible(true);
            field.set(builder, interceptors);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static List<Interceptor> removeDuplicate(List<Interceptor> list) {
        LinkedHashSet<Interceptor> h = new LinkedHashSet<>(list);
        h.addAll(interceptors);
        list.clear();
        list.addAll(h);
        return list;
    }
}