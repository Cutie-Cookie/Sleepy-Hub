package top.niunaijun.blackboxa.view.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import top.niunaijun.blackboxa.view.apps.AppsFragment

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private var fragmentList = mutableListOf<AppsFragment>()

    fun replaceData(list: MutableList<AppsFragment>) {
        fragmentList = list.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    override fun getItemId(position: Int): Long = fragmentList[position].userID.toLong()

    override fun containsItem(itemId: Long): Boolean =
        fragmentList.any { it.userID.toLong() == itemId }
}
