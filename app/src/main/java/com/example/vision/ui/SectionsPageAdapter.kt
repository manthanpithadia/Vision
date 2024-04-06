package com.example.vision.ui
import android.content.Context
import android.content.Intent
import android.graphics.Camera
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.vision.R
import com.example.vision.tensorflow.ObjectDetectionActivity
import com.example.vision.ui.fragment.CameraFragment
import com.example.vision.ui.fragment.HomeFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_home,
    R.string.tab_camera
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if(position==0) return HomeFragment.newInstance(position + 1)
        else return HomeFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return TAB_TITLES.size
    }
}