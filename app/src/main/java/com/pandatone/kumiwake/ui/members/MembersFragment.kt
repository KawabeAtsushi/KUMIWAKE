package com.pandatone.kumiwake.ui.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.ui.settings.MembersViewModel

class MembersFragment : Fragment() {

    private lateinit var membersViewModel: MembersViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        membersViewModel =
                ViewModelProviders.of(this).get(MembersViewModel::class.java)
        return inflater.inflate(R.layout.fragment_members, container, false)
    }
}