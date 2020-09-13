package ru.ivmak.raspisanie_iktib.ui.screens.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.textfield.TextInputEditText
import ru.ivmak.raspisanie_iktib.*
import ru.ivmak.raspisanie_iktib.data.Choice
import ru.ivmak.raspisanie_iktib.data.TimeTable
import ru.ivmak.raspisanie_iktib.ui.rv_adapters.DraverRvAdapter
import ru.ivmak.raspisanie_iktib.ui.screens.settings.SettingsActivity
import ru.ivmak.raspisanie_iktib.utils.Constants
import ru.ivmak.raspisanie_iktib.utils.Functions

class MainActivity : AppCompatActivity(),
    DraverRvAdapter.OnItemClickListener {

    private var menu: Menu? = null
    lateinit var toolbar: Toolbar
    private var adapter = DraverRvAdapter(arrayListOf(),this)
    private lateinit var draverRV: RecyclerView

    private lateinit var sPref: SharedPreferences

    private lateinit var selectWeekBtn: Button
    private lateinit var nextWeekBtn: ImageButton
    private lateinit var privWeekBtn: ImageButton
    private lateinit var weekManageLayout: LinearLayout

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var selecttimeTableBtn: Button
    private lateinit var failImage: ImageView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var searchEditText: TextInputEditText

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sPref = getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE)

        viewPager = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.sliding_tabs)

        nextWeekBtn = findViewById(R.id.next_week_btn)
        privWeekBtn = findViewById(R.id.priv_week_btn)
        selectWeekBtn = findViewById(R.id.select_week_btn)
        weekManageLayout = findViewById(R.id.week_layout)
        appBarLayout = findViewById(R.id.appBarLayout)
        failImage = findViewById(R.id.not_connect_image)

        drawerLayout = findViewById(R.id.drawer_layout)

        selecttimeTableBtn = findViewById<Button>(R.id.select_timetable_btn)
        selecttimeTableBtn.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) { }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) { }

            override fun onDrawerOpened(drawerView: View) { }

            override fun onDrawerClosed(drawerView: View) {
                val imm = this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(this@MainActivity.window.decorView.windowToken, 0)
            }

        })
        toggle.syncState()

        viewPager.adapter =
            SimpleFragmentPagerAdapter(
                supportFragmentManager
            )
        tabLayout.setupWithViewPager(viewPager)

        draverRV = findViewById(R.id.draver_rv)
        draverRV.layoutManager = LinearLayoutManager(this)
        draverRV.adapter = adapter

        viewModel.choices.observe(this, Observer {
            val textIsEmpty = findViewById<TextView>(R.id.text_empty)
            if (it.isEmpty()) {
                if (viewModel.timeTable.value?.result == Constants.CONNECTION_FAIL) {
                    textIsEmpty.text = "Нет доступа к интернету, проверьте соединение"
                } else {
                    textIsEmpty.text = "Ничего не найдено"
                }
                textIsEmpty.visibility = View.VISIBLE
            } else {
                textIsEmpty.visibility = View.INVISIBLE
            }
            adapter.setData(it)
            adapter.notifyDataSetChanged()
        })

        viewModel.timeTable.observe(this, Observer {
            initAppBar(it)
        })

        searchEditText = findViewById<TextInputEditText>(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchGroups(s.toString())
            }

        })

        nextWeekBtn.setOnClickListener {
            selectWeekBtn.isEnabled = false
            nextWeekBtn.isEnabled = false
            privWeekBtn.isEnabled = false
            getData(
                viewModel.timeTable.value!!.table!!.group,
                viewModel.timeTable.value!!.weeks!![viewModel.timeTable.value!!.weeks!!.indexOf(viewModel.timeTable.value!!.table!!.week) + 1]
            )
        }
        privWeekBtn.setOnClickListener {
            selectWeekBtn.isEnabled = false
            nextWeekBtn.isEnabled = false
            privWeekBtn.isEnabled = false
            getData(
                viewModel.timeTable.value!!.table!!.group,
                viewModel.timeTable.value!!.weeks!![viewModel.timeTable.value!!.weeks!!.indexOf(viewModel.timeTable.value!!.table!!.week) - 1]
            )
        }
        selectWeekBtn.setOnClickListener {
            selectWeekBtn.isEnabled = false
            nextWeekBtn.isEnabled = false
            privWeekBtn.isEnabled = false

            showWeekPopUp()
        }

        initNotifications()

        GlobalScope.launch {
            viewModel.initTimeTable()
        }
    }

    private fun initNotifications() {
        val time = sPref.getString(Constants.PREF_NOTIF_TIME, "")
        if (time == "") {
            sPref.edit()
                .putString(
                    Constants.PREF_NOTIF_TIME,
                    Constants.DEF_NOTIF_TIME
                )
                .apply()

            Functions.scheduleNotification(
                this,
                Functions.getDuration(Constants.DEF_NOTIF_TIME)
            )
        }
    }

    private fun showWeekPopUp() {
        val popup =
            PopupMenu(this@MainActivity, this@MainActivity.findViewById(R.id.select_week_btn)) //item!!.actionView
        for (i in viewModel.timeTable.value!!.weeks!!) {
            popup.menu.add("$i неделя")
        }
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            getData(
                viewModel.timeTable.value!!.table!!.group,
                Integer.parseInt(it.title.substring(0, it.title.indexOf(' ')))
            )
            true
        }

        popup.show()
    }

    override fun onChoiseItemClick(data: Choice) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        searchEditText.text?.clear()
        getData(data.group)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun initAppBar(timeTable: TimeTable) {

        provideResult(timeTable)

        if (timeTable.table != null) {
            weekManageLayout.visibility = View.VISIBLE
            appBarLayout.visibility = View.VISIBLE
            viewPager.visibility = View.VISIBLE
            selecttimeTableBtn.visibility = View.GONE
            failImage.visibility = View.GONE

            val viewPager = findViewById<ViewPager>(R.id.viewpager)

            val curDay = Functions.isDayOfWeekOpen(timeTable.table!!)
            if (curDay != -1) {
                viewPager.currentItem = curDay
            }

            selectWeekBtn.text = timeTable.table!!.week.toString() + " неделя"
            selectWeekBtn.isEnabled = true
            if (timeTable.weeks!!.indexOf(timeTable.table!!.week) < timeTable.weeks!!.size - 1) {
                nextWeekBtn.isEnabled = true
            }

            if (timeTable.weeks!!.indexOf(timeTable.table!!.week) > 0) {
                privWeekBtn.isEnabled = true
            }
        }
    }

    private fun provideResult(timeTable: TimeTable) {
        when (timeTable.result) {
            Constants.CONNECTION_FAIL -> {
                title = "[offline]"
                Toast.makeText(this, "Не удалось подключиться к интернету, проверьте соединение", Toast.LENGTH_SHORT).show()
            }
            Constants.SERVER_ERROR -> {
                title = "[offline]"
                Toast.makeText(this, "Не удалось подключиться к серверу, попробуйте позже", Toast.LENGTH_SHORT).show()
            }
            Constants.SP_EMPTY -> {
                title = timeTable.table?.name ?: "Расписание ИКТИБ"
                weekManageLayout.visibility = View.GONE
                appBarLayout.visibility = View.GONE
                viewPager.visibility = View.GONE
                selecttimeTableBtn.visibility = View.VISIBLE
                failImage.visibility = View.VISIBLE
            }
            Constants.RESULT_OK -> {
                title = timeTable.table?.name ?: "Расписание ИКТИБ"
            }
            else -> {
                title = timeTable.table?.name ?: "Расписание ИКТИБ"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
//            R.id.select_week -> {
//                showWeekPopUp()
//            }
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchGroups(query: String) {
        GlobalScope.launch {
            viewModel.searchByQuery(query)
        }
    }

    private fun getData(group: String) {
        GlobalScope.launch {
            viewModel.getTimeTable(group)
        }
    }

    private fun getData(group: String, week: Int) {
        GlobalScope.launch {
            viewModel.getTimeTableByWeek(group, week)
        }
    }
}
