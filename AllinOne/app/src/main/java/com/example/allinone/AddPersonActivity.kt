package com.example.allinone

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddPersonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_person)

        val etName = findViewById<EditText>(R.id.et_person_name_input)
        
        findViewById<View>(R.id.btn_close_add_person).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btn_save_person).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                val newLedger = PersonalLedger(personName = name)
                DataManager.personalLedgers.add(0, newLedger)
                DataManager.saveData(this)
                finish()
            } else {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
