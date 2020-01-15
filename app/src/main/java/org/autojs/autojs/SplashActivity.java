package org.autojs.autojs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.autojs.autojs.ui.main.MainActivity_;
import org.autojs.autojs.ui.user.LoginActivity;
import org.autojs.autojs.util.Constant;
import org.autojs.autojs.util.SPUtils;
import org.autojs.autojs.view.LoginActivityAp;


public class SplashActivity extends AppCompatActivity {


    private Handler handler=new Handler();
    private Runnable runnable;
    private int countdown = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //不显示程序的标题栏
        requestWindowFeature( Window.FEATURE_NO_TITLE );

        //不显示系统的标题栏
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.act_splash);
        initRunnable();
    }

    private void initRunnable () {
        runnable = new Runnable() {
            @Override
            public void run() {
                countdown--;
                Log.i("test1","countdown = " + countdown);
                if(countdown==0){
                    handler.removeCallbacks(runnable);
                    String token = SPUtils.getSharedStringData(SplashActivity.this, Constant.Token);
                    Intent intent = new Intent();
                    if (null == token || token.equals("")) {
                        intent.setClass(SplashActivity.this, LoginActivityAp.class);
                        startActivity(intent);
//                        ARouterPath.JumpCallBack(ARouterPath.LoginPath, SplashActivity.this, 0, false);
                    } else {
                        intent.setClass(SplashActivity.this, MainActivity_.class);
                        startActivity(intent);
//                        ARouterPath.JumpCallBack(ARouterPath.MainPath, SplashActivity.this, 0, false);
                    }
                    finish();
                }else{
                    handler.postDelayed(runnable,1 * 1000);
                }

            }
        };
        handler.postDelayed(runnable, 2 * 1000);
    }
}
