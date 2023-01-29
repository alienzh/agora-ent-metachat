package io.agora.metachat.home.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogDownloadBinding

/**
 * @author create by zhangwei03
 */
class MChatDownloadDialog constructor() : BaseFragmentDialog<MchatDialogDownloadBinding>() {

    private var cancelCallback: (() -> Unit)? = null

    fun setCancelCallback(cancelCallback: () -> Unit) = apply {
        this.cancelCallback = cancelCallback
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogDownloadBinding {
        return MchatDialogDownloadBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding?.mbCancel?.setOnClickListener {
            cancelCallback?.invoke()
        }
    }

    fun updateProgress(progress: Int) {
        if (progress < 0 || progress > 100) return
        binding?.apply {
            tvProgress.text = "$progress%"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(progress, true)
            } else {
                progressBar.progress = progress
            }
        }
    }
}