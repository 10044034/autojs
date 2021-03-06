package org.autojs.autojs.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.Nullable;

import org.autojs.autojs.R;
import org.autojs.autojs.bean.LoginBean;
import org.autojs.autojs.retrofit.network.repository.FdKyAppDataRepository;
import org.autojs.autojs.ui.main.MainActivity_;
import org.autojs.autojs.util.Constant;
import org.autojs.autojs.util.SPUtils;
import org.autojs.autojs.util.SwitchButton;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class LoginActivityAp extends Activity {

    private ImageView m_obj_changeAccount;
    private boolean m_b_isRememberPwd;
    private EditText m_account_et;
    private EditText m_password_et;
    private EditText m_deviceId_et;
    private Button m_btn_login;

    private SwitchButton switchButton;
//    private SwitchButton switchButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        m_account_et = this.findViewById(R.id.id_login_layout_loginname);
        m_password_et = this.findViewById(R.id.id_login_layout_password);
        m_deviceId_et = this.findViewById(R.id.id_login_device_id_password);
        m_btn_login = findViewById(R.id.login_btn);
        m_btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        SPUtils.setSharedStringData(this, Constant.JsExcuteType, Constant.JsExcuteTypeAccount);
        switchButton = findViewById(R.id.sb_ios);
        switchButton.setChecked(false);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int a = 5;
                m_b_isRememberPwd = isChecked;
                if (isChecked) {
                    // 收款
                    SPUtils.setSharedStringData(LoginActivityAp.this, Constant.JsExcuteType, Constant.JsExcuteTypePay);
                } else {
                    // 账单
                    SPUtils.setSharedStringData(LoginActivityAp.this, Constant.JsExcuteType, Constant.JsExcuteTypeAccount);
                }
            }
        });
//        switchButton = findViewById(R.id.sb_ios);
//        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//            }
//        });

    }

    public void login() {
        if (m_account_et.getText().toString().equals("")) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (m_password_et.getText().toString().equals("")) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (m_deviceId_et.getText().toString().equals("")) {
            Toast.makeText(this, "设备Id不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        LoadingDialog1.showDialogForLoading(this);

        FdKyAppDataRepository.Login(m_account_et.getText().toString(),
                m_password_et.getText().toString(), m_deviceId_et.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<LoginBean>() {
                    @Override
                    public void accept(LoginBean loginBean) throws Exception {
                        LoadingDialog1.cancelDialogForLoading();
                        if (loginBean.getStatus().equals("0")) {
                            Toast.makeText(LoginActivityAp.this, loginBean.getMessage() , Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int a = 5;
                        SPUtils.setSharedStringData(LoginActivityAp.this, Constant.CodeId, loginBean.getResult().getData().getCode_id());
                        SPUtils.setSharedStringData(LoginActivityAp.this, Constant.bankId, loginBean.getResult().getData().getBank_id());
                        SPUtils.setSharedStringData(LoginActivityAp.this, Constant.Token, loginBean.getResult().getToken());
                        Intent intent = new Intent();
                        intent.setClass(LoginActivityAp.this, MainActivity_.class);
                        startActivity(intent);
                        finish();

//                        mLiveObservableDataLoginBean.setValue(loginBean);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LoadingDialog1.cancelDialogForLoading();
                        int a = 5;
                    }
                });

    }


}
