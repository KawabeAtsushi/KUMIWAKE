package com.pandatone.kumiwake.ui.members

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.AddGroup
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.MemberMain
import com.pandatone.kumiwake.ui.DialogWarehouse
import com.pandatone.kumiwake.ui.settings.MembersViewModel

class MembersFragment : Fragment() {

    private lateinit var membersViewModel: MembersViewModel
    private val dialog: DialogWarehouse
        get() {
            return DialogWarehouse(requireFragmentManager())
        }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        membersViewModel =
                ViewModelProviders.of(this).get(MembersViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_members, container, false)

        val memberListButton: TextView = root.findViewById(R.id.member_list_button)
        memberListButton.setOnClickListener {
            startActivity(Intent(activity, MemberMain::class.java))
        }
        val addMemberButton: TextView = root.findViewById(R.id.add_member_button)
        addMemberButton.setOnClickListener {
            startActivity(Intent(activity, AddMember::class.java))
        }
        val addGroupButton: TextView = root.findViewById(R.id.add_group_button)
        addGroupButton.setOnClickListener {
            startActivity(Intent(activity, AddGroup::class.java))
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.help_icon_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_help -> dialog.confirmationDialog(getString(R.string.hint) + " : " + getString(R.string.member), getString(R.string.how_to_member))
        }
        return true
    }
}