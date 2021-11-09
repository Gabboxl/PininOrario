package ga.gabboxl.pininorario

import android.app.Application
import com.google.android.material.color.DynamicColors

class PininOrario : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}