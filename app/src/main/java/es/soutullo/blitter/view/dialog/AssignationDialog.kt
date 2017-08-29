package es.soutullo.blitter.view.dialog

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import com.buildware.widget.indeterm.IndeterminateCheckBox
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.person.Person
import es.soutullo.blitter.view.adapter.generic.GenericListAdapter
import es.soutullo.blitter.view.dialog.generic.CustomLayoutDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

class AssignationDialog(context: Context, handler: IDialogHandler, title: String, private val billLines: List<BillLine>,
                        private val people: List<Person>) : CustomLayoutDialog(context, handler, title) {

    private fun generateCheckBoxesStatus(): Array<Boolean>? {
        // TODO implement here
        return null
    }

    override fun getCustomView(): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPositiveText(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareConcreteDialog(dialogBuilder: AlertDialog.Builder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class AssignationDialogAdapter(private val peopleCheckBoxesStates: Array<Boolean?>) : GenericListAdapter<Person>() {

        override fun getItemLayout(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        private inner class AssignationDialogViewHolder(itemView: View) : GenericListAdapter<Person>.GenericListViewHolder(itemView) {
            private val checkBox: IndeterminateCheckBox? = null
            private val canBeIndeterminate: Boolean = false
        }
    }
}