package com.example.allinone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MenuAdapter
    private lateinit var menuItems: MutableList<MenuItem>

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { exportToFile(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importFromFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("MainActivity", "onCreate called")
        setContentView(R.layout.activity_main)

        // Load data from persistent storage
        DataManager.loadData(this)

        val dateTextView = findViewById<TextView>(R.id.date_textview)
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        dateTextView.text = currentDate

        findViewById<View>(R.id.btn_back).visibility = View.GONE
        findViewById<ImageButton>(R.id.btn_main_menu).setOnClickListener { showMainMenu(it) }

        val menuGrid = findViewById<RecyclerView>(R.id.menu_grid)
        menuGrid.layoutManager = GridLayoutManager(this, 2)

        menuItems = mutableListOf(
            MenuItem("Habit Tracker", R.drawable.ic_habit_tracker, HabitTrackerActivity::class.java, colorResId = R.color.logo_salmon),
            MenuItem("Workout Routine", R.drawable.ic_workout_routine, WorkoutRoutineActivity::class.java, colorResId = R.color.logo_yellow),
            MenuItem("To-Do List", R.drawable.ic_todo_list, ToDoListActivity::class.java, showProgress = false, colorResId = R.color.logo_teal),
            MenuItem("Notes", R.drawable.ic_notes, NotesActivity::class.java, showProgress = false, colorResId = R.color.logo_blue),
            MenuItem("Project", R.drawable.ic_project, ProjectActivity::class.java, showProgress = false, colorResId = R.color.primary_blue),
            MenuItem("Finance", R.drawable.ic_finance, FinanceActivity::class.java, showProgress = false, colorResId = R.color.card_blue)
        )

        adapter = MenuAdapter(this, menuItems)
        menuGrid.adapter = adapter
    }

    private fun showMainMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_main_menu, null)

        val popupWindow = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        menuView.findViewById<View>(R.id.menu_export).setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            exportLauncher.launch("AllInOne_Backup_$timestamp.json")
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_import).setOnClickListener {
            importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_about).setOnClickListener {
            // About logic here
            popupWindow.dismiss()
        }

        // Show popup below the anchor button
        popupWindow.showAsDropDown(anchor, -300, 0)
    }

    private fun exportToFile(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val json = DataManager.exportData()
                outputStream.write(json.toByteArray())
            }
            Toast.makeText(this, "Full app backup saved successfully!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importFromFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.readText()
                if (DataManager.importData(this, json)) {
                    updateAllProgress()
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "All data restored successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Restore failed: Invalid file format", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateAllProgress()
    }

    private fun updateAllProgress() {
        // Update individual menu items
        adapter.updateProgress("Habit Tracker", DataManager.getHabitProgress())
        adapter.updateProgress("Workout Routine", DataManager.getWorkoutProgress())
    }
}
