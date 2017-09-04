package es.soutullo.blitter.view.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityBillPersonTraceBinding
import es.soutullo.blitter.model.vo.person.Person
import es.soutullo.blitter.view.adapter.BillPersonTraceAdapter
import es.soutullo.blitter.view.util.BlitterUtils

class BillPersonTraceActivity : AppCompatActivity() {
    companion object {
        val PERSON_INTENT_DATA_KEY = "EXTRA_PERSON"
    }

    private val linesAdapter = BillPersonTraceAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val binding = DataBindingUtil.setContentView<ActivityBillPersonTraceBinding>(this, R.layout.activity_bill_person_trace)
        val person = this.intent.getSerializableExtra(PERSON_INTENT_DATA_KEY) as Person

        binding.utils = BlitterUtils
        binding.person = person

        this.findViewById<RecyclerView>(R.id.bill_trace_list).adapter = this.linesAdapter
        this.linesAdapter.addAll(person.lines)
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }
}
