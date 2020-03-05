package wgnelther.faces.learn

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences("Preferences", 0)
    private val editor = preferences.edit()

    //Init Session
    //
    fun clearSession() {
        editor.clear()
        editor.apply()
    }


    fun putName(
            name: String
    ) {
        editor.putString("Name", name)
        editor.apply()
    }


    //Get Session
    //
    fun getName(): String? {
        return preferences.getString("Name", "null")
    }
}


