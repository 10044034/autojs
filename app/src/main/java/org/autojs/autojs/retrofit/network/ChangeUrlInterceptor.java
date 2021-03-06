package org.autojs.autojs.retrofit.network;

import android.util.Log;


import com.stardust.app.GlobalAppContext;

import org.autojs.autojs.util.ApiConstants;
import org.autojs.autojs.util.Constant;
import org.autojs.autojs.util.SPUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

import static okhttp3.internal.Util.UTF_8;


/*
* 动态换baseUrl 拦截器
* 参考：https://www.jianshu.com/p/fa6da5168958
* 请求参数和响应参数获取 https://blog.csdn.net/qq_20089667/article/details/80618997
* */
public class ChangeUrlInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

        //获取原始的originalRequest
        Request originalRequest = chain.request();
        RequestBody requestBody;
//        printParams(originalRequest.body());

//        Response response =  chain.proceed(chain.request());
//        Log.e("request", "【请求参数】:" + bodyToString(originalRequest));
//        Log.e("net", "【响应参数】:" + responseToString(response));
//
//        /*拦截器获取状态码  是否跳回登录  https://www.jianshu.com/p/0868633f4de6*/
//
//        Log.e("network","CreateInterceptor request url "+response.request().url());
//        Log.e("network","CreateInterceptor  response code "+response.code());
//        if (response.code() == 400) {
//
//            EventBus.getDefault().post(new JumpToLoginEvent());
//            CreateInterceptorExceptioin interceptorExceptioin = new CreateInterceptorExceptioin();
//
//            interceptorExceptioin.setErrorCode(400);
//            interceptorExceptioin.setRetry_after(response.header("Retry-After"));
//            throw  interceptorExceptioin;
//        }

        originalRequest.body();
        //获取来的url
        HttpUrl oldUrl = originalRequest.url();
        //获取Request的创建者builder
        Request.Builder builder = originalRequest.newBuilder();
        // 从requests 中获取headers, 通过给定的健url_name
        List<String> headerValues = originalRequest.headers("url_name");
        String Authorization = SPUtils.getSharedStringData(GlobalAppContext.getsApplicationContext(), Constant.Token);
        System.out.println(" --------- ChangeUrlInterceptor Authorization ---------- " + Authorization);
        if (null != headerValues && headerValues.size() > 0) {
            builder.removeHeader("url_name");
            String urlName = headerValues.get(0);
            if (!oldUrl.encodedPath().contains("api/auth/token") && null != Authorization && !Authorization.equals("")) {
//                builder.addHeader("Authorization", originalRequest.header("Authorization"));
                builder.header("Authorization", Authorization);
            }

            /**********************************************/
//            Response response =  chain.proceed(chain.request());
//            Log.e("request", "【请求参数】:" + bodyToString(originalRequest));

            /*拦截器获取状态码  是否跳回登录  https://www.jianshu.com/p/0868633f4de6*/

//            Log.e("network","CreateInterceptor request url "+response.request().url());
//            Log.e("network","CreateInterceptor  response code "+response.code());
//            if (response.code() == 2001 && !oldUrl.encodedPath().equals("api/auth/token")) {
//
//                CreateInterceptorExceptioin interceptorExceptioin = new CreateInterceptorExceptioin();
//
//                interceptorExceptioin.setErrorCode(400);
//                interceptorExceptioin.setRetry_after(response.header("Retry-After"));
//                throw  interceptorExceptioin;
//            }
            /************************************************/
            HttpUrl newBaseUrl = null;
            if ("base_host".equals(urlName)) {
                newBaseUrl = HttpUrl.parse(ApiConstants.GankHost);
            } else if ("gd_map".equals(urlName)) {
                newBaseUrl = HttpUrl.parse(ApiConstants.GdHost);
            }
            Log.e("WZ", "newBaseUrl = " + newBaseUrl);

            HttpUrl newHttpUrl = oldUrl.newBuilder()
                    .scheme(newBaseUrl.scheme())
                    .host(newBaseUrl.host())
                    .port(newBaseUrl.port())
                    .build();
            Request newRequest = builder.url(newHttpUrl).build();
            return chain.proceed(newRequest);
        } else {
            return chain.proceed(originalRequest);
        }
    }


    private static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            RequestBody body = copy.body();
            if (body == null)
                return "";
            body.writeTo(buffer);
//            return getJsonString(buffer.readUtf8());
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "{\"err\": \"" + e.getMessage() + "\"}";
        }
    }



    private void printParams(RequestBody body) {
        Buffer buffer = new Buffer();
        try {
            body.writeTo(buffer);
            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = body.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF_8);
            }
            String params = buffer.readString(charset);
//            Log.e(TAG, "请求参数： | " + params);
            System.out.println("【请求参数】：" + params);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public class CreateInterceptorExceptioin extends Error{
        private int errorCode;
        private String retry_after;


        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public String getRetry_after() {
            return retry_after;
        }

        public void setRetry_after(String retry_after) {
            this.retry_after = retry_after;
        }
    }
}
