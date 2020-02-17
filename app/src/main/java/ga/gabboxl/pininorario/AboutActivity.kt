package ga.gabboxl.pininorario

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.sizeDp


class AboutActivity : MaterialAboutActivity() {
    override fun getMaterialAboutList(context: Context): MaterialAboutList {

        val miscCardBuilder: MaterialAboutCard.Builder = MaterialAboutCard.Builder()
        val authorCardBuilder: MaterialAboutCard.Builder = MaterialAboutCard.Builder()
        buildAuthor(context, authorCardBuilder)
        buildMisc(context, miscCardBuilder)


        return MaterialAboutList(miscCardBuilder.build(), authorCardBuilder.build()) // This creates an empty screen, add cards with .addCard()
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

    private fun buildAuthor(context: Context, authorCardBuilder: MaterialAboutCard.Builder) {
        authorCardBuilder.title("Autore")
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text("Gabriele")
            .subText("gabboxl")
            .icon(IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_account)
                .color(IconicsColor.colorInt(ContextCompat.getColor(context, R.color.mal_color_icon_light_theme)))
                .sizeDp(18))
            .setOnClickAction{
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://gabboxl.ga")
                    context.startActivity(i) }
            .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text("PininOrario su Github")
                .icon(IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_github_circle)
                    .color(IconicsColor.colorInt(ContextCompat.getColor(context, R.color.mal_color_icon_light_theme)))
                    .sizeDp(18))
                .setOnClickAction{val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse("https://github.com/gabboxl/PininOrario")
                    context.startActivity(i)}
                .build())
    }



    override fun getActivityTitle(): CharSequence? {
        return getString(R.string.title_activity_about)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}
