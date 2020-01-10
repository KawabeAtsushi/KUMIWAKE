package com.pandatone.kumiwake.ui.sekigime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.pandatone.kumiwake.R

class SekigimeFragment : Fragment() {

    private lateinit var sekigimeViewModel: SekigimeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        sekigimeViewModel =
                ViewModelProviders.of(this).get(SekigimeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_sekigime, container, false)
    }
}