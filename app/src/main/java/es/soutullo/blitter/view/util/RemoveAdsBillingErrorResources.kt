package es.soutullo.blitter.view.util

import es.soutullo.blitter.R
import es.soutullo.blitter.model.billing.RemoveAdsBillingError

fun RemoveAdsBillingError.messageResId(): Int {
    return when (this) {
        RemoveAdsBillingError.FEATURE_NOT_SUPPORTED -> R.string.toast_purchase_feature_not_supported
        RemoveAdsBillingError.SERVICE_DISCONNECTED -> R.string.toast_purchase_service_disconnected
        RemoveAdsBillingError.SERVICE_UNAVAILABLE -> R.string.toast_purchase_service_unavailable
        RemoveAdsBillingError.BILLING_UNAVAILABLE -> R.string.toast_purchase_billing_unavailable
        RemoveAdsBillingError.PRODUCT_UNAVAILABLE -> R.string.toast_purchase_product_unavailable
        RemoveAdsBillingError.DEVELOPER_ERROR -> R.string.toast_purchase_developer_error
        RemoveAdsBillingError.ITEM_ALREADY_OWNED -> R.string.toast_purchase_item_already_owned
        RemoveAdsBillingError.ITEM_NOT_OWNED -> R.string.toast_purchase_item_not_owned
        RemoveAdsBillingError.NETWORK_ERROR -> R.string.toast_purchase_network_error
        RemoveAdsBillingError.UNKNOWN -> R.string.toast_purchase_unknown_error
    }
}
