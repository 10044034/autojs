package org.autojs.autojs.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.stardust.app.GlobalAppContext;

import org.autojs.autojs.App;
import org.autojs.autojs.bean.SubmitResponse;
import org.autojs.autojs.retrofit.network.repository.FdKyAppDataRepository;
import org.autojs.autojs.util.Constant;
import org.autojs.autojs.util.SPUtils;
import org.greenrobot.eventbus.EventBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by dell on 2018/4/4.
 */

public class SubmitDataBroadcast extends BroadcastReceiver{
    public static String submitData = "com.submit.data";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().contentEquals(submitData)) {


//            String data = intent.getStringExtra("sumitData");
            int a = 5;
            String codeId = SPUtils.getSharedStringData(GlobalAppContext.getsApplicationContext(), Constant.CodeId);
            String bankId = SPUtils.getSharedStringData(GlobalAppContext.getsApplicationContext(), Constant.bankId);
            String token = SPUtils.getSharedStringData(GlobalAppContext.getsApplicationContext(), Constant.Token);
            String data = intent.getStringExtra("sumitData");
//            EventBus.getDefault().post(new PayMentDataEvent("数据上传中。。。"));

            FdKyAppDataRepository.submitData(codeId, bankId, token, data)
                    .subscribeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<SubmitResponse>() {
                        @Override
                        public void accept(SubmitResponse submitResponse) throws Exception {

//                            mUploadStatus.setText("上传成功");
                            if (submitResponse.getStatus().equals("1")) {
                                Toast.makeText(GlobalAppContext.getsApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(GlobalAppContext.getsApplicationContext(), submitResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
//                            mUploadStatus.setText("上传失败");
//                            EventBus.getDefault().post(new PayMentDataEvent("上传失败"));
                            Toast.makeText(GlobalAppContext.getsApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
