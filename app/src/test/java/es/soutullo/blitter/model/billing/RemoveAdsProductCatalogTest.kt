package es.soutullo.blitter.model.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoveAdsProductCatalogTest {
    @Test
    fun productIdsContainsAllPlayConsoleRemoveAdsProducts() {
        assertEquals(
                listOf(
                        "es.soutullo.blitter.product.removeads.basicmeal",
                        "es.soutullo.blitter.product.removeads.soda",
                        "es.soutullo.blitter.product.removeads",
                        "es.soutullo.blitter.product.removeads.nicemeal",
                        "es.soutullo.blitter.product.removeads.cry"
                ),
                RemoveAdsProductCatalog.removeAdsProductIds
        )
    }

    @Test
    fun supportProductIdsContainsAllPlayConsoleSupportProducts() {
        assertEquals(
                listOf(
                        "es.soutullo.blitter.product.support.basicmeal",
                        "es.soutullo.blitter.product.support.soda",
                        "es.soutullo.blitter.product.support.coffee",
                        "es.soutullo.blitter.product.support.nicemeal",
                        "es.soutullo.blitter.product.support.cry"
                ),
                RemoveAdsProductCatalog.supportProductIds
        )
    }

    @Test
    fun containsAnyAcceptsAnyRemoveAdsProduct() {
        RemoveAdsProductCatalog.removeAdsProductIds.forEach { productId ->
            assertTrue(RemoveAdsProductCatalog.containsAny(listOf(productId)))
        }
    }

    @Test
    fun containsAnyRejectsSupportProducts() {
        RemoveAdsProductCatalog.supportProductIds.forEach { productId ->
            assertFalse(RemoveAdsProductCatalog.containsAny(listOf(productId)))
        }
    }

    @Test
    fun containsAnyRejectsUnrelatedProducts() {
        assertFalse(RemoveAdsProductCatalog.containsAny(listOf("es.soutullo.blitter.product.other")))
    }

    @Test
    fun kindOfClassifiesRemoveAdsAndSupportProducts() {
        assertEquals(BillingProductKind.REMOVE_ADS, RemoveAdsProductCatalog.kindOf(RemoveAdsProductCatalog.COFFEE))
        assertEquals(BillingProductKind.SUPPORT, RemoveAdsProductCatalog.kindOf(RemoveAdsProductCatalog.SUPPORT_COFFEE))
        assertEquals(null, RemoveAdsProductCatalog.kindOf("es.soutullo.blitter.product.other"))
    }

    @Test
    fun productOptionDisplayNameUsesContributionLabelWhenPresent() {
        val option = RemoveAdsProductOption(
                productId = RemoveAdsProductCatalog.COFFEE,
                name = "Remove all the ads and waits (Buy me a coffee)",
                formattedPrice = "EUR 0.99",
                priceAmountMicros = 990_000L
        )

        assertEquals("Buy me a coffee", option.displayName)
    }

    @Test
    fun productOptionDisplayNameTrimsContributionLabel() {
        val option = RemoveAdsProductOption(
                productId = RemoveAdsProductCatalog.BASIC_MEAL,
                name = "Remove all the ads and waits (Pay my meal )",
                formattedPrice = "EUR 4.99",
                priceAmountMicros = 4_990_000L
        )

        assertEquals("Pay my meal", option.displayName)
    }

    @Test
    fun productOptionDisplayNameFallsBackToPlayName() {
        val option = RemoveAdsProductOption(
                productId = RemoveAdsProductCatalog.COFFEE,
                name = "Remove ads",
                formattedPrice = "EUR 0.99",
                priceAmountMicros = 990_000L
        )

        assertEquals("Remove ads", option.displayName)
    }
}
