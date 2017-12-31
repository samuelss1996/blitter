package es.soutullo.blitter.view.activity

import android.content.Intent
import android.support.v4.app.Fragment
import es.soutullo.blitter.R

class AssignationIntroActivity : ABlitterIntroActivity() {
    override val titleId = R.string.assignation_intro_title
    override val descriptionId = R.string.assignation_intro_description
    override val drawableId = R.drawable.ic_list_white_128dp
    override val mainColorId = R.color.md_green_600
    override val barColorId = R.color.md_green_800
    override val preferenceKey = FLAG_ASSIGNATION_INTRO_VIEWED

    companion object {
        val FLAG_ASSIGNATION_INTRO_VIEWED = "FLAG_INTRO_ASSIGNATION"
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        val bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY)
        this.startActivity(Intent(this, AssignationActivity::class.java).putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, bill))
    }
}