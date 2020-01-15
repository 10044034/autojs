package org.autojs.autojs.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.stardust.app.FragmentPagerAdapterBuilder;
import com.stardust.app.GlobalAppContext;
import com.stardust.app.OnActivityResultDelegate;
import com.stardust.autojs.core.permission.OnRequestPermissionsResultCallback;
import com.stardust.autojs.core.permission.PermissionRequestProxyActivity;
import com.stardust.autojs.core.permission.RequestPermissionCallbacks;
import com.stardust.enhancedfloaty.FloatyService;
import com.stardust.pio.PFiles;
import com.stardust.theme.ThemeColorManager;
import com.stardust.util.BackPressedHandler;
import com.stardust.util.DeveloperUtils;
import com.stardust.util.DrawerAutoClose;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.autojs.autojs.BuildConfig;
import org.autojs.autojs.Pref;
import org.autojs.autojs.R;
import org.autojs.autojs.autojs.AutoJs;
import org.autojs.autojs.bean.PingResponse;
import org.autojs.autojs.broadcast.SubmitDataBroadcast;
import org.autojs.autojs.event.FileLoadSuccessEvent;
import org.autojs.autojs.external.foreground.ForegroundService;
import org.autojs.autojs.file.FileUtil;
import org.autojs.autojs.model.explorer.Explorers;
import org.autojs.autojs.model.script.ScriptFile;
import org.autojs.autojs.model.script.Scripts;
import org.autojs.autojs.retrofit.network.repository.FdKyAppDataRepository;
import org.autojs.autojs.tool.AccessibilityServiceTool;
import org.autojs.autojs.ui.BaseActivity;
import org.autojs.autojs.ui.common.NotAskAgainDialog;
import org.autojs.autojs.ui.floating.FloatyWindowManger;
import org.autojs.autojs.ui.log.LogActivity_;
import org.autojs.autojs.ui.main.community.CommunityFragment;
import org.autojs.autojs.ui.settings.SettingsActivity_;
import org.autojs.autojs.ui.update.VersionGuard;
import org.autojs.autojs.ui.widget.CommonMarkdownView;
import org.autojs.autojs.ui.widget.SearchViewItem;
import org.autojs.autojs.util.Constant;
import org.autojs.autojs.util.SPUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static org.autojs.autojs.broadcast.SubmitDataBroadcast.submitData;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity implements OnActivityResultDelegate.DelegateHost, BackPressedHandler.HostActivity, PermissionRequestProxyActivity {

    public static class DrawerOpenEvent {
        static DrawerOpenEvent SINGLETON = new DrawerOpenEvent();
    }

    private static final String LOG_TAG = "MainActivityAp";

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.viewpager)
    ViewPager mViewPager;

    @ViewById(R.id.fab)
    FloatingActionButton mFab;


    private Observable<Long> mObservable;

    private Disposable mdisposable = null;

    private FragmentPagerAdapterBuilder.StoredFragmentPagerAdapter mPagerAdapter;
    private OnActivityResultDelegate.Mediator mActivityResultMediator = new OnActivityResultDelegate.Mediator();
    private RequestPermissionCallbacks mRequestPermissionCallbacks = new RequestPermissionCallbacks();
    private VersionGuard mVersionGuard;
    private BackPressedHandler.Observer mBackPressObserver = new BackPressedHandler.Observer();
    private SearchViewItem mSearchViewItem;
    private MenuItem mLogMenuItem;
    private boolean mDocsSearchItemExpanded;

    private String saveJsFile = "test5.js";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        mVersionGuard = new VersionGuard(this);
        showAnnunciationIfNeeded();
        EventBus.getDefault().register(this);
        applyDayNightMode();
        initBroadCast();

        mObservable = Observable.interval(0, 1, TimeUnit.SECONDS);


        boolean is = showAccessibilitySettingPromptIfDisabled();
        if (is) {
            Ping();
            interval(15*1000);
        } else {
            startCountDown();
        }

//        interval2(10*1000);

//        Scripts.INSTANCE.run(new ScriptFile("/data/user/0/org.autojs.autojs/files/sample/安卓7.0+点按和手势/心形手势.js"));
    }




    /*
     * 心跳
     * */
    private void Ping () {
        String codeId = SPUtils.getSharedStringData(GlobalAppContext.getsApplicationContext(), Constant.CodeId);
        String bankId = SPUtils.getSharedStringData(GlobalAppContext.getsApplicationContext(), Constant.bankId);
        String token = SPUtils.getSharedStringData(GlobalAppContext.getsApplicationContext(), Constant.Token);
        FdKyAppDataRepository.Ping(codeId, bankId, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PingResponse>() {
                    @Override
                    public void accept(PingResponse pingResponse) throws Exception {
                        if (pingResponse.getStatus().equals("0")) {
                            Toast.makeText(MainActivity.this, pingResponse.getMessage() , Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (pingResponse.getResult().getNeed_data().equals("1")) {
//                            Toast.makeText(MainActivityAp.this, "开始抓取", Toast.LENGTH_SHORT).show();
//                            Intent broadCastIntent = new Intent();
//                            broadCastIntent.setAction(AlipayBroadcast.BillPageAppRefreshBrodCast);
//                            sendBroadcast(broadCastIntent);

                            Scripts.INSTANCE.run(new ScriptFile("/data/user/0/org.autojs.autojs/cache/" + saveJsFile));
//                            Scripts.INSTANCE.run(new ScriptFile("/data/user/0/org.autojs.autojs/files/sample/文件上传1.js"));

                        } else {
//                            Scripts.INSTANCE.run(new ScriptFile("/data/user/0/org.autojs.autojs/cache/" + saveJsFile));
//                            Scripts.INSTANCE.run(new ScriptFile("/data/user/0/org.autojs.autojs/files/sample/文件上传1.js"));
                            Toast.makeText(MainActivity.this, "不需要抓取", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEventJumpToLogin(FileLoadSuccessEvent event) {

    }

    /** 每隔milliseconds毫秒后执行next操作
     *
     * @param milliseconds
     */
    public void interval(long milliseconds){
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {

                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mdisposable=disposable;
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Long number) {
                        Toast.makeText(MainActivity.this, "判断是否需要重新抓取", Toast.LENGTH_SHORT).show();
                        Ping();

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                        Toast.makeText(MainActivity.this, "开始采集onComplete", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Disposable countDownDisposable = null;
    private void startCountDown () {
        //开始倒计时
        final int count = 15;//倒计时3秒id_close_video_audio_tips
        mObservable.take(count + 1)//限制发射次数（因为倒计时要显示 3 2 1 0 四个数字）
                //使用map将数字转换，这里aLong是从0开始增长的,所以减去aLong就会输出3 2 1 0这种样式
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        return count - aLong;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        countDownDisposable = d;
                    }

                    @Override
                    public void onNext(Long num) {
                        //接收到消息，这里需要判空，因为3秒倒计时中间如果页面结束了，会造成找不到 tvAdCountDown

                    }

                    @Override
                    public void onError(Throwable e) {


                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(MainActivity.this, "onComplete", Toast.LENGTH_SHORT).show();
                        boolean is = showAccessibilitySettingPromptIfDisabled();
                        if (is) {
                            FileUtil.copy(MainActivity.this, "arr1.js", MainActivity.this.getCacheDir().getAbsolutePath(), saveJsFile);
                            Ping();
                            interval(15*1000);
                            if (null != countDownDisposable && !countDownDisposable.isDisposed()) {
                                countDownDisposable.dispose();
                            }
                        } else {
                            startCountDown();
                        }

                    }
                });
    }


    @AfterViews
    void setUpViews() {
        setUpToolbar();
        setUpTabViewPager();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        registerBackPressHandlers();
        ThemeColorManager.addViewBackground(findViewById(R.id.app_bar));
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                EventBus.getDefault().post(DrawerOpenEvent.SINGLETON);
            }
        });
    }
    private SubmitDataBroadcast submitDataBroadcast;
    private void initBroadCast() {
        submitDataBroadcast = new SubmitDataBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(submitData);
        this.registerReceiver(submitDataBroadcast, intentFilter);
    }
    private void showAnnunciationIfNeeded() {
        if (!Pref.shouldShowAnnunciation()) {
            return;
        }
//        new CommonMarkdownView.DialogBuilder(this)
//                .padding(36, 0, 36, 0)
//                .markdown(PFiles.read(getResources().openRawResource(R.raw.annunciation)))
//                .title(R.string.text_annunciation)
//                .positiveText(R.string.ok)
//                .canceledOnTouchOutside(false)
//                .show();
    }


    private void registerBackPressHandlers() {
        mBackPressObserver.registerHandler(new DrawerAutoClose(mDrawerLayout, Gravity.START));
        mBackPressObserver.registerHandler(new BackPressedHandler.DoublePressExit(this, R.string.text_press_again_to_exit));
    }

    private void checkPermissions() {
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean showAccessibilitySettingPromptIfDisabled() {
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(this)) {
            return true;
        }
        new NotAskAgainDialog.Builder(this, "MainActivityAp.accessibility")
                .title(R.string.text_need_to_enable_accessibility_service)
                .content(R.string.explain_accessibility_permission)
                .positiveText(R.string.text_go_to_setting)
                .negativeText(R.string.text_cancel)
                .onPositive((dialog, which) ->
                        AccessibilityServiceTool.enableAccessibilityService()
                ).show();
        return false;
    }

    private void setUpToolbar() {
        Toolbar toolbar = $(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.text_drawer_open,
                R.string.text_drawer_close);
        drawerToggle.syncState();
        mDrawerLayout.addDrawerListener(drawerToggle);
    }

    private void setUpTabViewPager() {
        TabLayout tabLayout = $(R.id.tab);
//        mPagerAdapter = new FragmentPagerAdapterBuilder(this)
//                .add(new MyScriptListFragment_(), R.string.text_file)
//                .add(new DocsFragment_(), R.string.text_tutorial)
//                .add(new CommunityFragment_(), R.string.text_community)
//                .add(new MarketFragment(), R.string.text_market)
//                .add(new TaskManagerFragment_(), R.string.text_manage)
//                .build();
//        mPagerAdapter = new FragmentPagerAdapterBuilder(this)
//                .add(new MyScriptListFragment_(), R.string.text_file)
//                .build();
//        mViewPager.setAdapter(mPagerAdapter);
//        tabLayout.setupWithViewPager(mViewPager);
//        setUpViewPagerFragmentBehaviors();
    }

    private void setUpViewPagerFragmentBehaviors() {
        mPagerAdapter.setOnFragmentInstantiateListener((pos, fragment) -> {
            ((ViewPagerFragment) fragment).setFab(mFab);
            if (pos == mViewPager.getCurrentItem()) {
                ((ViewPagerFragment) fragment).onPageShow();
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            private ViewPagerFragment mPreviousFragment;

            @Override
            public void onPageSelected(int position) {
                Fragment fragment = mPagerAdapter.getStoredFragment(position);
                if (fragment == null)
                    return;
                if (mPreviousFragment != null) {
                    mPreviousFragment.onPageHide();
                }
                mPreviousFragment = (ViewPagerFragment) fragment;
                mPreviousFragment.onPageShow();
            }
        });
    }


    @Click(R.id.setting)
    void startSettingActivity() {
        startActivity(new Intent(this, SettingsActivity_.class));
    }

    @Click(R.id.exit)
    public void exitCompletely() {
        finish();
        FloatyWindowManger.hideCircularMenu();
        ForegroundService.stop(this);
        stopService(new Intent(this, FloatyService.class));
        AutoJs.getInstance().getScriptEngineService().stopAll();
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVersionGuard.checkForDeprecatesAndUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mActivityResultMediator.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mRequestPermissionCallbacks.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }
        if (getGrantResult(Manifest.permission.READ_EXTERNAL_STORAGE, permissions, grantResults) == PackageManager.PERMISSION_GRANTED) {
            Explorers.workspace().refreshAll();
        }
    }

    private int getGrantResult(String permission, String[] permissions, int[] grantResults) {
        int i = Arrays.asList(permissions).indexOf(permission);
        if (i < 0) {
            return 2;
        }
        return grantResults[i];
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!BuildConfig.DEBUG) {
            DeveloperUtils.verifyApk(this, R.string.dex_crcs);
        }
    }


    @NonNull
    @Override
    public OnActivityResultDelegate.Mediator getOnActivityResultDelegateMediator() {
        return mActivityResultMediator;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = mPagerAdapter.getStoredFragment(mViewPager.getCurrentItem());
        if (fragment instanceof BackPressedHandler) {
            if (((BackPressedHandler) fragment).onBackPressed(this)) {
                return;
            }
        }
        if (!mBackPressObserver.onBackPressed(this)) {
            super.onBackPressed();
        }
    }

    @Override
    public void addRequestPermissionsCallback(OnRequestPermissionsResultCallback callback) {
        mRequestPermissionCallbacks.addCallback(callback);
    }

    @Override
    public boolean removeRequestPermissionsCallback(OnRequestPermissionsResultCallback callback) {
        return mRequestPermissionCallbacks.removeCallback(callback);
    }


    @Override
    public BackPressedHandler.Observer getBackPressedObserver() {
        return mBackPressObserver;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mLogMenuItem = menu.findItem(R.id.action_log);
        setUpSearchMenuItem(searchMenuItem);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log) {
            if (mDocsSearchItemExpanded) {
                submitForwardQuery();
            } else {
                LogActivity_.intent(this).start();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onLoadUrl(CommunityFragment.LoadUrl loadUrl) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    private void setUpSearchMenuItem(MenuItem searchMenuItem) {
        mSearchViewItem = new SearchViewItem(this, searchMenuItem) {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (mViewPager.getCurrentItem() == 1) {
                    mDocsSearchItemExpanded = true;
                    mLogMenuItem.setIcon(R.drawable.ic_ali_up);
                }
                return super.onMenuItemActionExpand(item);
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (mDocsSearchItemExpanded) {
                    mDocsSearchItemExpanded = false;
                    mLogMenuItem.setIcon(R.drawable.ic_ali_log);
                }
                return super.onMenuItemActionCollapse(item);
            }
        };
        mSearchViewItem.setQueryCallback(this::submitQuery);
    }

    private void submitQuery(String query) {
        if (query == null) {
            EventBus.getDefault().post(QueryEvent.CLEAR);
            return;
        }
        QueryEvent event = new QueryEvent(query);
        EventBus.getDefault().post(event);
        if (event.shouldCollapseSearchView()) {
            mSearchViewItem.collapse();
        }
    }

    private void submitForwardQuery() {
        QueryEvent event = QueryEvent.FIND_FORWARD;
        EventBus.getDefault().post(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mdisposable && !mdisposable.isDisposed()) {
            mdisposable.dispose();
        }
        if (null != countDownDisposable && !countDownDisposable.isDisposed()) {
            countDownDisposable.dispose();
        }
        if (null != submitDataBroadcast) {
            this.unregisterReceiver(submitDataBroadcast);
        }
        EventBus.getDefault().unregister(this);
    }






    /**
     * 设置代理信息 exclList是添加不用代理的网址用的
     * */
    ProxyInfo mInfo;
    public void setHttpPorxySetting(Context context, String host, int port, List<String> exclList)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, NoSuchFieldException {
        WifiManager wifiManager =(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(wifiManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mInfo = ProxyInfo.buildDirectProxy(host,port);
        }
        if (config != null){
            Class clazz = Class.forName("android.net.wifi.WifiConfiguration");
            Class parmars = Class.forName("android.net.ProxyInfo");
            Method method = clazz.getMethod("setHttpProxy",parmars);
            method.invoke(config,mInfo);
            Object mIpConfiguration = getDeclaredFieldObject(config,"mIpConfiguration");

            setEnumField(mIpConfiguration, "STATIC", "proxySettings");
            setDeclardFildObject(config,"mIpConfiguration",mIpConfiguration);
            //save the settings
            wifiManager.updateNetwork(config);
            wifiManager.disconnect();
            wifiManager.reconnect();
        }

    }

    // 获取当前的Wifi连接
    public static WifiConfiguration getCurrentWifiConfiguration(WifiManager wifiManager) {
        if (!wifiManager.isWifiEnabled())
            return null;
        List<WifiConfiguration> configurationList = wifiManager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = wifiManager.getConnectionInfo().getNetworkId();
        // Log.d("当前wifi连接信息",wifiManager.getConnectionInfo().toString());
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == cur)
                configuration = wifiConfiguration;
        }
        return configuration;
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException,IllegalArgumentException, IllegalAccessException{

        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    public static Object getDeclaredFieldObject(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj); return out;
    }
    public static void setDeclardFildObject(Object obj,String name,Object object){
        Field f = null;
        try {
            f = obj.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        f.setAccessible(true);
        try {
            f.set(obj,object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 取消代理设置
     * */
    public void unSetHttpProxy(Context context)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException,
            NoSuchFieldException, NoSuchMethodException {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration configuration = getCurrentWifiConfiguration(wifiManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mInfo = ProxyInfo.buildDirectProxy(null,0);
        }
        if (configuration != null){
            Class clazz = Class.forName("android.net.wifi.WifiConfiguration");
            Class parmars = Class.forName("android.net.ProxyInfo");
            Method method = clazz.getMethod("setHttpProxy",parmars);
            method.invoke(configuration,mInfo);
            Object mIpConfiguration = getDeclaredFieldObject(configuration,"mIpConfiguration");
            setEnumField(mIpConfiguration, "NONE", "proxySettings");
            setDeclardFildObject(configuration,"mIpConfiguration",mIpConfiguration);

            //保存设置
            wifiManager.updateNetwork(configuration);
            wifiManager.disconnect();
            wifiManager.reconnect();
        }
    }
    /*************************************************************************/
}