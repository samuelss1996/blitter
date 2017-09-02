package es.soutullo.blitter.view.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.Menu
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityFinalResultBinding
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.model.vo.person.Person
import es.soutullo.blitter.view.adapter.FinalResultAdapter
import java.util.*

class FinalResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFinalResultBinding
    private lateinit var bill: Bill

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_final_result)
        this.bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill

        this.init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_app_bar_final_result, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    fun onSplitAgainClicked() {
        // TODO implement here
    }

    fun onDoneClicked() {
        // TODO implement here
    }

    fun onRenameClicked() {
        // TODO implement here
    }


    fun onRenamed(newName: String) {
        this.bill.name = newName
        this.supportActionBar?.title = newName
        this.doBackup()
    }

    fun onBillPersonClicked(listIndex: Int, person: Person) {
        // TODO implement here
    }

    private fun doBackup() {
        this.bill.status = EBillStatus.COMPLETED
        DaoFactory.getFactory(this).getBillDao().updateBill(this.bill.id, this.bill)
    }

    private fun init() {
        val adapter = FinalResultAdapter()

        this.onRenamed(this.getString(R.string.bill_final_name_pattern, DateFormat.getDateFormat(this).format(Date())))
        this.binding.bill = this.bill

        this.findViewById<RecyclerView>(R.id.final_result_list).adapter = adapter

        adapter.add(null)
        adapter.addAll(this.bill.lines.map { it.persons }.flatten().distinct() )
    }
}
