package io.agora.metachat.home

import android.view.LayoutInflater
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.databinding.MchatActivityMainBinding

class MChatMainActivity : BaseUiActivity<MchatActivityMainBinding>() {
    override fun getViewBinding(inflater: LayoutInflater): MchatActivityMainBinding? {
        return MchatActivityMainBinding.inflate(inflater)
    }
}