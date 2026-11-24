package com.example.vita.data

object FirestorePaths {

    const val USERS = "users"
    const val DASHBOARD = "dashboard"
    const val SUMMARY = "summary"
    const val PROFILE = "profile"
    const val REFEICOES = "refeicoes"
    const val AGUA = "agua"

    fun usuario(uid: String): String {
        return "$USERS/$uid"
    }

    fun refeicoes(uid: String): String {
        return "$USERS/$uid/$REFEICOES"
    }

    fun agua(uid: String): String {
        return "$USERS/$uid/$AGUA"
    }

    fun dashboardResumo(uid: String): String {
        return "$USERS/$uid/$DASHBOARD/$SUMMARY"
    }

    fun dashboardPerfil(uid: String): String {
        return "$USERS/$uid/$DASHBOARD/$PROFILE"
    }
}