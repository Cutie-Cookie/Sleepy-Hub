package top.niunaijun.blackboxa.view.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import top.niunaijun.blackboxa.view.apps.AppsFragment

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private var fragmentList = mutableListOf<AppsFragment>()

    /**
     * Keep the existing Fragment instance for any profile that remains in this pager.
     * FragmentStateAdapter keeps fragments by stable item id, so MainActivity must use
     * this same canonical list when it looks up a profile's live fragment.
     */
    fun replaceData(list: List<AppsFragment>) {
        val existingByUserId = fragmentList.associateBy { it.userID }
        fragmentList = list.map { requested ->
            existingByUserId[requested.userID] ?: requested
        }.toMutableList()
        notifyDataSetChanged()
    }

    fun fragments(): List<AppsFragment> = fragmentList.toList()

    fun fragmentAt(position: Int): AppsFragment? = fragmentList.getOrNull(position)

    fun indexOfUserId(userId: Int): Int =
        fragmentList.indexOfFirst { it.userID == userId }

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    override fun getItemId(position: Int): Long = fragmentList[position].userID.toLong()

    override fun containsItem(itemId: Long): Boolean =
        fragmentList.any { it.userID.toLong() == itemId }
}
