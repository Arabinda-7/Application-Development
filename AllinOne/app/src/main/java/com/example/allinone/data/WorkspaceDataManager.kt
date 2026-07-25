package com.example.allinone.data

import com.example.allinone.AgendaItem

object WorkspaceDataManager {
    var workspaceTodayAgenda = mutableMapOf<String, List<AgendaItem>>()
    var lastViewedNotificationDate: String = ""
    var lastSummaryNotificationDate: String = ""
}
