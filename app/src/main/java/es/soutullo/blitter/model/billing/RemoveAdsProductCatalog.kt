package es.soutullo.blitter.model.billing

object RemoveAdsProductCatalog {
    const val BASIC_MEAL = "es.soutullo.blitter.product.removeads.basicmeal"
    const val SODA = "es.soutullo.blitter.product.removeads.soda"
    const val COFFEE = "es.soutullo.blitter.product.removeads"
    const val NICE_MEAL = "es.soutullo.blitter.product.removeads.nicemeal"
    const val CRY = "es.soutullo.blitter.product.removeads.cry"

    const val SUPPORT_BASIC_MEAL = "es.soutullo.blitter.product.support.basicmeal"
    const val SUPPORT_SODA = "es.soutullo.blitter.product.support.soda"
    const val SUPPORT_COFFEE = "es.soutullo.blitter.product.support.coffee"
    const val SUPPORT_NICE_MEAL = "es.soutullo.blitter.product.support.nicemeal"
    const val SUPPORT_CRY = "es.soutullo.blitter.product.support.cry"

    val removeAdsProductIds = listOf(
            BASIC_MEAL,
            SODA,
            COFFEE,
            NICE_MEAL,
            CRY
    )

    val supportProductIds = listOf(
            SUPPORT_BASIC_MEAL,
            SUPPORT_SODA,
            SUPPORT_COFFEE,
            SUPPORT_NICE_MEAL,
            SUPPORT_CRY
    )

    fun containsAny(products: List<String>): Boolean {
        return products.any { it in removeAdsProductIds }
    }

    fun kindOf(productId: String): BillingProductKind? {
        return when (productId) {
            in removeAdsProductIds -> BillingProductKind.REMOVE_ADS
            in supportProductIds -> BillingProductKind.SUPPORT
            else -> null
        }
    }
}

enum class BillingProductKind {
    REMOVE_ADS,
    SUPPORT
}

data class RemoveAdsProductOption(
        val productId: String,
        val name: String,
        val formattedPrice: String,
        val priceAmountMicros: Long,
        val kind: BillingProductKind = BillingProductKind.REMOVE_ADS
) {
    val displayName: String
        get() = CONTRIBUTION_LABEL_REGEX.find(name)?.groupValues?.get(1)?.trim() ?: name

    companion object {
        private val CONTRIBUTION_LABEL_REGEX = Regex("\\(([^)]+)\\)")
    }
}
