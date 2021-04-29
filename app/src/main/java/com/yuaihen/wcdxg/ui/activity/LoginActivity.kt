package com.yuaihen.wcdxg.ui.activity

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import cn.leancloud.AVUser
import com.gyf.immersionbar.ImmersionBar
import com.yuaihen.wcdxg.AppManager
import com.yuaihen.wcdxg.R
import com.yuaihen.wcdxg.base.BaseActivity
import com.yuaihen.wcdxg.databinding.ActivityLoginBinding
import com.yuaihen.wcdxg.utils.LogUtil
import com.yuaihen.wcdxg.utils.UserUtil
import com.yuaihen.wcdxg.utils.invisible
import com.yuaihen.wcdxg.utils.visible
import io.reactivex.disposables.Disposable


/**
 * Created by Yuaihen.
 * on 2021/3/27
 * 登录页面
 */
class LoginActivity : BaseActivity(), TextView.OnEditorActionListener {

    private lateinit var binding: ActivityLoginBinding

    override fun getBindingView(): View {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun initData() {
        binding.progressBar.invisible()
        binding.btnLogin.setOnClickListener {
            AppManager.getInstance().hideSoftKeyBoard(this)
            verificationAccountAndPwd()
        }
        binding.btnRegister.setOnClickListener {
            start2Activity(RegisterActivity::class.java)
        }
        binding.editTextVerificationCode.setOnEditorActionListener(this)
        binding.editTextAccount.addTextChangedListener(watcher)
        binding.editTextPwd.addTextChangedListener(watcher)
        binding.editTextVerificationCode.addTextChangedListener(watcher)
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == KeyEvent.ACTION_DOWN || actionId == EditorInfo.IME_ACTION_DONE) {
            verificationAccountAndPwd()
            return true
        }
        return false
    }


    private val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val t1 = binding.editTextAccount.text.toString().trim().isNotEmpty()
            val t2 = binding.editTextPwd.text.toString().trim().isNotEmpty()
            val t3 = binding.editTextVerificationCode.text.toString().trim().isNotEmpty()
            binding.btnLogin.isEnabled = t1 and t2 and t3
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }


    private fun verificationAccountAndPwd() {
        val userName = binding.editTextAccount.text?.trim().toString()
        val pwd = binding.editTextPwd.text?.trim().toString()
        val verificationCode = binding.editTextVerificationCode.text.trim().toString()
        if (userName.isEmpty() || pwd.isEmpty() || verificationCode.isEmpty()) {
            toast(R.string.login_fail)
            return
        } else {
            loginByLeanCloud(userName, pwd)
        }
    }

    /**
     * 验证账号密码
     * 使用LeanCloud保存用户账号信息
     */
    private fun loginByLeanCloud(userName: String, pwd: String) {
        binding.progressBar.visible()
        AVUser.logIn(userName, pwd).subscribe(object : io.reactivex.Observer<AVUser> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: AVUser) {
                UserUtil.setUserIsLogin(true)
                toast(R.string.login_success)
                start2Activity(MainActivity::class.java, finish = true)
            }

            override fun onError(e: Throwable) {
                if (e.message!!.contains("Could not find user")) {
                    toast("用户未注册")
                    start2Activity(RegisterActivity::class.java)
                } else {
                    toast("登录失败 ${e.message}")
                }
                LogUtil.d("hello", "onError: ${e.message}")
                binding.progressBar.visibility = View.INVISIBLE
            }

            override fun onComplete() {

            }

        })
    }


}