package bg.iag.tel24

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bg.iag.tel24.databinding.ActivityMainBinding
import bg.iag.tel24.ui.MainPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val tabs = listOf("ИАГ", "РДГ", "ДП", "Търси")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.viewPager.adapter = MainPagerAdapter(this)
        b.viewPager.offscreenPageLimit = 2

        TabLayoutMediator(b.tabLayout, b.viewPager) { tab, pos ->
            tab.text = tabs[pos]
        }.attach()
    }
}
