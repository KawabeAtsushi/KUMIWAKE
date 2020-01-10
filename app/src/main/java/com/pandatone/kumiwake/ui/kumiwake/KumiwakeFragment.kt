package com.pandatone.kumiwake.ui.kumiwake

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.pandatone.kumiwake.R

class KumiwakeFragment : Fragment() {

    private lateinit var kumiwakeViewModel: KumiwakeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        kumiwakeViewModel =
                ViewModelProviders.of(this).get(KumiwakeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_kumiwake, container, false)
    }
}