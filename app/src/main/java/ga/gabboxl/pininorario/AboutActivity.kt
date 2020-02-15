package ga.gabboxl.pininorario

import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom


class AboutActivity : MaterialAboutActivity() {
    override fun getMaterialAboutList(context: Context): MaterialAboutList {

        val miscCardBuilder: MaterialAboutCard.Builder = MaterialAboutCard.Builder()
        buildMisc(context, miscCardBuilder)


        return MaterialAboutList(miscCardBuilder.build()) // This creates an empty screen, add cards with .addCard()
    }

    private fun buildMisc(context: Context, miscCardBuilder: MaterialAboutCard.Builder){
        miscCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .icon(ContextCompat.getDrawable(context, R.drawable.ic_update))
            .text(R.string.version_about)
            .subText(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE+ ")")
            .setOnClickAction {
                AppUpdater(this)
                    .setDisplay(Display.DIALOG)
                    .setUpdateFrom(UpdateFrom.JSON)
                    .setUpdateJSON("https://pinin.gabboxl.ga/versions/update.json")
                    .setWebviewChangelog(true)
                    .showAppUpdated(true)
                    .setButtonDoNotShowAgain("")
                    .start()
            }
            .build())
            .addItem(MaterialAboutActionItem.Builder()
                .subText(R.string.credits_about)
                .build())

    }



    override fun getActivityTitle(): CharSequence? {
        return getString(R.string.title_activity_about)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}
