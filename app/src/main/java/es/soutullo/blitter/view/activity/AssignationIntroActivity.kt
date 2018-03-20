package es.soutullo.blitter.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill

class AssignationIntroActivity : ABlitterIntroActivity() {
    override val titleId = R.string.assignation_intro_title
    override val descriptionId = R.string.assignation_intro_description
    override val drawableId = R.drawable.ic_list_white_128dp
    override val mainColorId = R.color.md_green_600
    override val barColorId = R.color.md_green_800
    override val preferenceKey = FLAG_ASSIGNATION_INTRO_VIEWED

    private lateinit var bill: Bill

    companion object {
        const val FLAG_ASSIGNATION_INTRO_VIEWED = "FLAG_INTRO_ASSIGNATION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill

        this.updateBillId(this.bill.id)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        this.startActivityForResult(Intent(this, AssignationActivity::class.java)
                .putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, bill), BillSummaryActivity.RETURN_BILL_ID_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == BillSummaryActivity.RETURN_BILL_ID_CODE && resultCode == Activity.RESULT_OK && data != null) {
            this.updateBillId(data.getLongExtra(BillSummaryActivity.INTENT_DATA_RETURNED_BILL_ID, -1))
        }
    }

    private fun updateBillId(id: Long?) {
        if(id != null && id > 0) {
            this.bill.id = id
            this.setResult(Activity.RESULT_OK, Intent().putExtra(BillSummaryActivity.INTENT_DATA_RETURNED_BILL_ID, id))
        }
    }
}