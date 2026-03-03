package bg.iag.tel24.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import bg.iag.tel24.ui.search.SearchFragment
import bg.iag.tel24.ui.tree.TreeFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 4
    override fun createFragment(position: Int): Fragment = when (position) {
        0    -> TreeFragment.newIag()
        1    -> TreeFragment.newRdg()
        2    -> TreeFragment.newDp()
        3    -> SearchFragment()
        else -> TreeFragment.newIag()
    }
}
