package es.soutullo.blitter.view.dialog.data

import android.databinding.BaseObservable
import android.databinding.ObservableField

/**
 * Item class for the persons list of the assignation dialog. Contains some functionaly to manage the
 * checkbox status
 * @param name The person name
 * @param status The initial status of the checkbox. If null, the status is set to indeterminate
 */
class AssignationDialogPerson(var name: ObservableField<String>, var status: ObservableField<Boolean?> = ObservableField(null)): BaseObservable() {
    var canBeIndeterminate: Boolean = this.status.get() == null

    /** Changes the checkbox to the next status */
    fun changeStatus() {
        this.status.get().let { currentStatus ->
            if(currentStatus == null || (!currentStatus && !this.canBeIndeterminate)) {
                this.status.set(true)
            } else if (currentStatus) {
                this.status.set(false)
            } else if(!currentStatus) {
                this.status.set(if(this.canBeIndeterminate) null else true)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssignationDialogPerson

        if (name.get() != other.name.get()) return false

        return true
    }

    override fun hashCode(): Int = name.get().hashCode()
}