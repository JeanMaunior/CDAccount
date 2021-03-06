/*
 * Copyright (C) 2020 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.xuexiang.cdaccount.fragment.login;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.patternlockview.utils.ResourceUtils;
import com.andrognito.rxpatternlockview.RxPatternLockView;
import com.andrognito.rxpatternlockview.events.PatternLockCompleteEvent;
import com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent;
import com.xuexiang.cdaccount.R;
import com.xuexiang.cdaccount.activity.FindpasswdActivity;
import com.xuexiang.cdaccount.biometriclib.BiometricPromptManager;
import com.xuexiang.cdaccount.core.BaseFragment;
import com.xuexiang.cdaccount.utils.RandomUtils;
import com.xuexiang.cdaccount.utils.TokenUtils;
import com.xuexiang.cdaccount.utils.XToastUtils;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xaop.util.MD5Utils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xutil.app.ActivityUtils;

import java.util.List;
import java.util.Objects;

import butterknife.OnClick;
import io.reactivex.functions.Consumer;

import static android.content.Context.MODE_PRIVATE;


/**
 * 登录页面
 *
 * @author xuexiang
 * @since 2019-11-17 22:15
 */
@Page(anim = CoreAnim.none)
public class LoginGestureFragment extends BaseFragment {

    private String password_gesture;

    private PatternLockView mPatternLockView;

    /**
     * @return 返回为 null意为不需要导航栏
     */
    @Override
    protected TitleBar initTitle() {
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login_gesture;
    }


    @Override
    protected void initViews() {
        initSP();
        initLock();
//        checkFingerprint();
        initFingerPrint();
    }

    @SuppressLint("NonConstantResourceId")
    @SingleClick
    @OnClick({R.id.tv_other_login1, R.id.tv_forget_password, R.id.tv_user_protocol, R.id.tv_privacy_protocol})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_other_login1:
//                openPage(LoginNumberFragment.class, getActivity().getIntent().getExtras());
                openPage(LoginNumberFragment.class, false);
                break;
            case R.id.tv_forget_password:
                ActivityUtils.startActivity(FindpasswdActivity.class);
                break;
            case R.id.tv_user_protocol:
                XToastUtils.info("用户协议");
                break;
            case R.id.tv_privacy_protocol:
                XToastUtils.info("隐私政策");
                break;
            default:
                break;
        }
    }


    private void initSP() {
        SharedPreferences mSharedPreferences_gesture_login = Objects.requireNonNull(getActivity()).getSharedPreferences("gesture", MODE_PRIVATE);
        password_gesture = mSharedPreferences_gesture_login.getString("gesture_sign", "");
    }


    @SuppressLint("CheckResult")
    protected void initLock() {
        //获取控件对象
        mPatternLockView = (PatternLockView) findViewById(R.id.login_patter_lock_view);
        // n*n大小   3*3
        mPatternLockView.setDotCount(3);
        //没有点击时点的大小
        mPatternLockView.setDotNormalSize((int) ResourceUtils.getDimensionInPx(Objects.requireNonNull(getContext()), R.dimen.pattern_lock_dot_size));
        //点击时点的大小
        mPatternLockView.setDotSelectedSize((int) ResourceUtils.getDimensionInPx(getContext(), R.dimen.pattern_lock_dot_selected_size));
        //更改路径距离
        mPatternLockView.setPathWidth((int) ResourceUtils.getDimensionInPx(getContext(), R.dimen.pattern_lock_path_width));
        mPatternLockView.setAspectRatioEnabled(true);
        mPatternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
        mPatternLockView.setDotAnimationDuration(150);
        mPatternLockView.setPathEndAnimationDuration(100);
        mPatternLockView.setInStealthMode(false);
        mPatternLockView.setTactileFeedbackEnabled(true);
        mPatternLockView.setInputEnabled(true);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);


        RxPatternLockView.patternComplete(mPatternLockView)
                .subscribe(new Consumer<PatternLockCompleteEvent>() {
                    @Override
                    public void accept(PatternLockCompleteEvent patternLockCompleteEvent) {
                        Log.d(getClass().getName(), "Complete: " + Objects.requireNonNull(patternLockCompleteEvent.getPattern()).toString());
                    }
                });

        RxPatternLockView.patternChanges(mPatternLockView)
                .subscribe(new Consumer<PatternLockCompoundEvent>() {
                    @Override
                    public void accept(PatternLockCompoundEvent event) throws Exception {
                        if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_STARTED) {
                            Log.d(getClass().getName(), "Pattern drawing started");
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_PROGRESS) {
                            Log.d(getClass().getName(), "Pattern progress: " +
                                    PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_COMPLETE) {
                            Log.d(getClass().getName(), "Pattern complete: " +
                                    PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_CLEARED) {
                            Log.d(getClass().getName(), "Pattern has been cleared");
                        }
                    }
                });
    }

    //设置监听器
    private final PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Log.d(getClass().getName(), "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));
            //密码验证
            String patternToString = PatternLockUtils.patternToString(mPatternLockView, pattern);
            if (!TextUtils.isEmpty(patternToString)) {
                if (MD5Utils.encode(patternToString).equals(password_gesture)) {
                    //判断为正确
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    String token = RandomUtils.getRandomNumbersAndLetters(16);
                    TokenUtils.setToken(token);
//                    XToastUtils.success("密码正确");
//                    Intent intent = null;
//                    if(TokenUtils.hasToken()){
//                        intent = new Intent(getContext(), MainActivity.class);
//                        startActivity(intent);
//                    }else{
//                        String token = RandomUtils.getRandomNumbersAndLetters(16);
//                        TokenUtils.setToken(token);
//                        startActivity(intent);
//                    }

                    Objects.requireNonNull(getActivity()).finish();
                } else {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    XToastUtils.error("密码错误");
                }
            }
            //1s后清除图案
            new Handler().postDelayed(() -> mPatternLockView.clearPattern(), 1000);
        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };

    private void initFingerPrint() {
        BiometricPromptManager mManager = BiometricPromptManager.from(getActivity());
        if (mManager.isBiometricPromptEnable()) {
            mManager.authenticate(new BiometricPromptManager.OnBiometricIdentifyCallback() {
                @Override
                public void onUsePassword() {
                    Toast.makeText(getContext(), "onUsePassword", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSucceeded() {
                    getActivity().finish();
//                    Toast.makeText(getContext(), "onSucceeded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed() {

                    Toast.makeText(getContext(), "onFailed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int code, String reason) {

                    Toast.makeText(getContext(), "onError", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {

                    Toast.makeText(getContext(), "onCancel", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    public void onResume() {        //修改密码后重新加载
        super.onResume();
        initSP();
    }


//    private void checkFingerprint() {
//        String TAG = "checktest";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            // 构建对话框
//            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(getContext())
//                    .setTitle("指纹验证")
//                    .setDescription("请验证指纹")
//                    .setNegativeButton("取消", getActivity().getMainExecutor(), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Toast.makeText(getContext(), "取消验证", Toast.LENGTH_SHORT).show();
//                        }
//                    }).build();
//
//            // 指纹识别回调
//            BiometricPrompt.AuthenticationCallback authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
//                @Override
//                public void onAuthenticationError(int errorCode, CharSequence errString) {
//                    super.onAuthenticationError(errorCode, errString);
//                    Log.i(TAG, "onAuthenticationError: errorCode = " + errorCode + ", errString = " + errString);
//                }
//
//                @Override
//                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
//                    super.onAuthenticationSucceeded(result);
//                    Log.i(TAG, "onAuthenticationSucceeded:");
//                    getActivity().finish();
//                }
//
//                @Override
//                public void onAuthenticationFailed() {
//                    super.onAuthenticationFailed();
//                    Log.i(TAG, "onAuthenticationFailed:");
//                }
//            };
//
//            // 开始验证指纹
//            CancellationSignal cancellationSignal = new CancellationSignal();
//            biometricPrompt.authenticate(cancellationSignal, getActivity().getMainExecutor(), authenticationCallback);
//        }
//    }
}

