package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels

import com.example.allinone.data.model.*

class PersonalLedgerHubActivity : BaseActivity() {

    private val viewModel: PersonalLedgerViewModel by viewModels()
    
    private lateinit var listSection: PersonalLedgerListSection
    private lateinit var themeManager: PersonalLedgerThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_ledger_hub)

        findViewById<TextView>(R.id.tv_title).text = "Personal Ledgers"

        initSections()
        setupLogic()
    }

    private fun initSections() {
        listSection = PersonalLedgerListSection(this, findViewById(R.id.rv_people_list)) { ledger ->
            val intent = Intent(this, PersonalLedgerBookActivity::class.java)
            intent.putExtra("ledgerId", ledger.id)
            startActivity(intent)
        }

        themeManager = PersonalLedgerThemeManager(
            findViewById(R.id.personal_ledger_hub_aura_background),
            findViewById(R.id.btn_add_person_full)
        )
    }

    private fun setupLogic() {
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_add_person_full).setOnClickListener {
            startActivity(Intent(this, AddPersonActivity::class.java))
        }

        themeManager.applyTheme()
        setupKeyboardHandling(findViewById(R.id.personal_ledger_hub_root), findViewById(R.id.personal_ledger_hub_content_container))
    }

    override fun onResume() {
        super.onResume()
        listSection.update()
        themeManager.applyTheme()
    }
}
