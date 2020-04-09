package ru.ivmak.raspisanie_iktib.ui.screens.main


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.ivmak.raspisanie_iktib.ui.screens.main.PageFragment

class SimpleFragmentPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    val PAGE_COUNT = 6
    private val tabTitles = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб")



    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        return PageFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence {
        // генерируем заголовок в зависимости от позиции
        return tabTitles[position]
    }
}