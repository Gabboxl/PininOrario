package ga.gabboxl.pininorario

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
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
            .text(R.string.versione_about)
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
        authorCardBuilder.title(getString(R.string.autore_card_about))
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text("Gabriele")
            .subText("gabboxl")
            .icon(IconicsDrawable(this, CommunityMaterial.Icon.cmd_account).apply {
                //colorInt = ContextCompat.getColor(context, R.color.defaultTextColor) single icon colors are auto-assigned by the library :(
                sizeDp = 18
            })
            .setOnClickAction{
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://gabboxl.ga")
                    context.startActivity(i) }
            .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.pininorario_su_github_about))
                .icon(IconicsDrawable(this, CommunityMaterial.Icon2.cmd_github).apply {
                    //colorInt = ContextCompat.getColor(context, R.color.defaultTextColor) single icon colors are auto-assigned by the library :(
                    sizeDp = 18
                })
                .setOnClickAction{val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse("https://github.com/gabboxl/PininOrario")
                    context.startActivity(i)}
                .build())
    }



    override fun getActivityTitle(): CharSequence {
        return getString(R.string.title_activity_about)
    }
}
