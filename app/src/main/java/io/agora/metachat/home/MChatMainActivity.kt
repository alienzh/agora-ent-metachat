package io.agora.metachat.home

import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.LayoutInflater
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.databinding.MchatActivityMainBinding
import io.agora.metachat.tools.LogTools

class MChatMainActivity : BaseUiActivity<MchatActivityMainBinding>() {
    override fun getViewBinding(inflater: LayoutInflater): MchatActivityMainBinding? {
        return MchatActivityMainBinding.inflate(inflater)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        LogTools.d("MChatMainActivity===","onSaveInstanceState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        LogTools.d("MChatMainActivity===","onRestoreInstanceState")
    }


}