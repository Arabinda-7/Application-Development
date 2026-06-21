package com.example.allinone

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommunicationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communication)

        val communicationList = findViewById<RecyclerView>(R.id.communication_list)
        communicationList.layoutManager = LinearLayoutManager(this)
        
        findViewById<View>(R.id.btn_create_new_comm).setOnClickListener {
            // Logic to add a new communication entry
        }
    }
}
