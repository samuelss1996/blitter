package es.soutullo.blitter.model.billing

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class RemoveAdsBillingManager(
        context: Context,
        private val removeAdsProductIds: List<String>,
        private val supportProductIds: List<String> = emptyList(),
        private val listener: Listener
) {
    interface Listener {
        fun onBillingReady(options: List<RemoveAdsProductOption>)
        fun onBillingUnavailable(error: RemoveAdsBillingError)
        fun onPurchaseCompleted(kind: BillingProductKind)
        fun onPurchaseRestored()
        fun onPurchaseFailed(error: RemoveAdsBillingError)
        fun onPurchaseCancelled()
    }

    companion object {
        private const val TAG = "RemoveAdsBilling"
        private const val MAX_PURCHASE_CONFIRMATION_ATTEMPTS = 3
        private const val PURCHASE_CONFIRMATION_RETRY_DELAY_MILLIS = 1_000L
        private const val MAX_ACKNOWLEDGEMENT_ATTEMPTS = 3
        private const val ACKNOWLEDGEMENT_RETRY_DELAY_MILLIS = 5_000L
        private const val MAX_CONSUMPTION_ATTEMPTS = 3
        private const val CONSUMPTION_RETRY_DELAY_MILLIS = 5_000L
    }

    private val appContext = context.applicationContext
    private val productIds = removeAdsProductIds + supportProductIds
    private val removeAdsProductIdSet = removeAdsProductIds.toSet()
    private val supportProductIdSet = supportProductIds.toSet()
    private val productIdSet = productIds.toSet()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        handlePurchaseUpdate(billingResult, purchases)
    }
    private val billingClient = BillingClient.newBuilder(appContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                            .enableOneTimeProducts()
                            .build()
            )
            .enableAutoServiceReconnection()
            .build()

    private var productDetailsById = mapOf<String, ProductDetails>()
    private var offerDetailsByProductId = mapOf<String, ProductDetails.OneTimePurchaseOfferDetails>()
    private var destroyed = false
    private var connecting = false
    private var purchaseInProgress = false
    private var launchedProductId: String? = null
    private var purchaseConfirmationAttempts = 0

    fun start(onPurchasesRefreshed: ((RemoveAdsPurchaseStatus) -> Unit)? = null) {
        if (destroyed) {
            return
        }

        if (productIds.isEmpty()) {
            dispatch {
                onPurchasesRefreshed?.invoke(RemoveAdsPurchaseStatus.UNAVAILABLE)
                listener.onBillingUnavailable(RemoveAdsBillingError.PRODUCT_UNAVAILABLE)
            }
            return
        }

        if (billingClient.isReady) {
            queryProductDetails()
            refreshPurchases(onPurchasesRefreshed)
            return
        }

        if (connecting) {
            onPurchasesRefreshed?.invoke(RemoveAdsPurchaseStatus.UNAVAILABLE)
            return
        }

        connecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connecting = false
                if (destroyed) {
                    return
                }

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    refreshPurchases(onPurchasesRefreshed)
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    dispatch {
                        onPurchasesRefreshed?.invoke(RemoveAdsPurchaseStatus.UNAVAILABLE)
                        listener.onBillingUnavailable(billingResult.toBillingError())
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                connecting = false
                dispatch {
                    onPurchasesRefreshed?.invoke(RemoveAdsPurchaseStatus.UNAVAILABLE)
                    listener.onBillingUnavailable(RemoveAdsBillingError.SERVICE_DISCONNECTED)
                }
            }
        })
    }

    fun launchPurchase(activity: Activity, productId: String) {
        if (!billingClient.isReady) {
            failPurchaseLaunch(RemoveAdsBillingError.SERVICE_DISCONNECTED)
            return
        }

        val details = productDetailsById[productId]
        val offerToken = offerDetailsByProductId[productId]?.offerToken
        if (details == null || offerToken == null) {
            failPurchaseLaunch(RemoveAdsBillingError.PRODUCT_UNAVAILABLE)
            return
        }

        purchaseInProgress = true
        launchedProductId = productId
        purchaseConfirmationAttempts = 0

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
        val billingResult = billingClient.launchBillingFlow(activity, flowParams)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "Billing flow failed: ${billingResult.debugMessage}")
            purchaseInProgress = false
            launchedProductId = null
            listener.onPurchaseFailed(billingResult.toBillingError())
        }
    }

    fun refreshPurchases(onResult: ((RemoveAdsPurchaseStatus) -> Unit)? = null) {
        if (!billingClient.isReady) {
            start(onResult)
            return
        }

        val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (destroyed) {
                return@queryPurchasesAsync
            }

            val queryResult = billingResult.toPurchaseQueryResult(purchases, removeAdsProductIdSet)
            when (queryResult.status) {
                RemoveAdsPurchaseStatus.OWNED -> {
                    processCompletedPurchase(queryResult.purchase!!, PurchaseSource.RESTORE) {
                        onResult?.invoke(RemoveAdsPurchaseStatus.OWNED)
                    }
                }
                RemoveAdsPurchaseStatus.NOT_OWNED -> dispatch {
                    onResult?.invoke(RemoveAdsPurchaseStatus.NOT_OWNED)
                }
                RemoveAdsPurchaseStatus.UNAVAILABLE -> {
                    Log.w(TAG, "Purchase refresh failed: ${billingResult.debugMessage}")
                    dispatch { onResult?.invoke(RemoveAdsPurchaseStatus.UNAVAILABLE) }
                }
            }
        }
    }

    fun destroy() {
        destroyed = true
        mainHandler.removeCallbacksAndMessages(null)

        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    private fun queryProductDetails() {
        val products = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (destroyed) {
                return@queryProductDetailsAsync
            }

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Product details query failed: ${billingResult.debugMessage}")
                dispatch { listener.onBillingUnavailable(billingResult.toBillingError()) }
                return@queryProductDetailsAsync
            }

            val availableProductDetails = productDetailsResult.productDetailsList
            this.productDetailsById = availableProductDetails.associateBy { it.productId }
            this.offerDetailsByProductId = availableProductDetails.mapNotNull { details ->
                details.preferredOneTimePurchaseOffer()?.let { offer -> details.productId to offer }
            }.toMap()

            val options = availableProductDetails
                    .mapNotNull { it.toRemoveAdsProductOption() }
                    .sortedByPriceAndCatalogOrder()

            dispatch {
                if (options.isEmpty()) {
                    listener.onBillingUnavailable(RemoveAdsBillingError.PRODUCT_UNAVAILABLE)
                } else {
                    listener.onBillingReady(options)
                }
            }
        }
    }

    private fun handlePurchaseUpdate(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (destroyed) {
            return
        }

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases.findCompletedKnownPurchase(launchedProductId)
                if (purchase == null) {
                    confirmPurchaseOutcome(RemoveAdsBillingError.UNKNOWN)
                } else {
                    processCompletedPurchase(purchase, PurchaseSource.NEW_PURCHASE)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                purchaseInProgress = false
                launchedProductId = null
                dispatch { listener.onPurchaseCancelled() }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                val source = if (launchedProductId in supportProductIdSet) {
                    PurchaseSource.NEW_PURCHASE
                } else {
                    PurchaseSource.RESTORE
                }
                confirmPurchaseOutcome(RemoveAdsBillingError.ITEM_ALREADY_OWNED, source)
            }
            else -> {
                Log.w(TAG, "Purchase update failed: ${billingResult.debugMessage}")
                confirmPurchaseOutcome(billingResult.toBillingError())
            }
        }
    }

    private fun confirmPurchaseOutcome(
            fallbackError: RemoveAdsBillingError,
            successSource: PurchaseSource = PurchaseSource.NEW_PURCHASE
    ) {
        queryKnownPurchase { queryResult ->
            when (queryResult.status) {
                RemoveAdsPurchaseStatus.OWNED -> processCompletedPurchase(queryResult.purchase!!, successSource)
                RemoveAdsPurchaseStatus.NOT_OWNED -> retryOrFailPurchaseConfirmation(fallbackError, successSource)
                RemoveAdsPurchaseStatus.UNAVAILABLE -> retryOrFailPurchaseConfirmation(fallbackError, successSource)
            }
        }
    }

    private fun retryOrFailPurchaseConfirmation(
            fallbackError: RemoveAdsBillingError,
            successSource: PurchaseSource
    ) {
        purchaseConfirmationAttempts++

        if (purchaseConfirmationAttempts >= MAX_PURCHASE_CONFIRMATION_ATTEMPTS) {
            failPurchase(fallbackError)
            return
        }

        mainHandler.postDelayed({
            if (!destroyed && purchaseInProgress) {
                confirmPurchaseOutcome(fallbackError, successSource)
            }
        }, PURCHASE_CONFIRMATION_RETRY_DELAY_MILLIS)
    }

    private fun failPurchase(error: RemoveAdsBillingError) {
        purchaseInProgress = false
        launchedProductId = null
        purchaseConfirmationAttempts = 0
        dispatch { listener.onPurchaseFailed(error) }
    }

    private fun processCompletedPurchase(
            purchase: Purchase,
            source: PurchaseSource,
            onProcessed: (() -> Unit)? = null
    ) {
        when (purchase.kind()) {
            BillingProductKind.REMOVE_ADS -> {
                grantPurchase(BillingProductKind.REMOVE_ADS, source, onProcessed)
                acknowledgePurchaseIfNeeded(purchase)
            }
            BillingProductKind.SUPPORT -> consumeSupportPurchase(purchase)
            null -> failPurchase(RemoveAdsBillingError.PRODUCT_UNAVAILABLE)
        }
    }

    private fun grantPurchase(
            kind: BillingProductKind,
            source: PurchaseSource,
            onProcessed: (() -> Unit)?
    ) {
        purchaseInProgress = false
        launchedProductId = null
        purchaseConfirmationAttempts = 0

        dispatch {
            when (source) {
                PurchaseSource.NEW_PURCHASE -> listener.onPurchaseCompleted(kind)
                PurchaseSource.RESTORE -> listener.onPurchaseRestored()
            }
            onProcessed?.invoke()
        }
    }

    private fun failPurchaseLaunch(error: RemoveAdsBillingError) {
        listener.onPurchaseFailed(error)
        start()
    }

    private fun ProductDetails.toRemoveAdsProductOption(): RemoveAdsProductOption? {
        val offerDetails = offerDetailsByProductId[this.productId] ?: return null

        return RemoveAdsProductOption(
                productId = this.productId,
                name = this.name.ifBlank { this.title },
                formattedPrice = offerDetails.formattedPrice,
                priceAmountMicros = offerDetails.priceAmountMicros,
                kind = RemoveAdsProductCatalog.kindOf(this.productId) ?: return null
        )
    }

    private fun List<RemoveAdsProductOption>.sortedByPriceAndCatalogOrder(): List<RemoveAdsProductOption> {
        return sortedWith(compareBy<RemoveAdsProductOption> { it.priceAmountMicros }
                .thenBy { productIds.indexOf(it.productId).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE })
    }

    private fun ProductDetails.preferredOneTimePurchaseOffer(): ProductDetails.OneTimePurchaseOfferDetails? {
        return this.oneTimePurchaseOfferDetailsList
                ?.filter { it.offerToken != null }
                ?.minByOrNull { it.priceAmountMicros }
                ?: this.oneTimePurchaseOfferDetails?.takeIf { it.offerToken != null }
    }

    private fun acknowledgePurchaseIfNeeded(purchase: Purchase, attempt: Int = 1) {
        if (purchase.isAcknowledged) {
            return
        }

        val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            if (destroyed || billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                return@acknowledgePurchase
            }

            Log.w(TAG, "Purchase acknowledgement failed: ${billingResult.debugMessage}")

            if (attempt < MAX_ACKNOWLEDGEMENT_ATTEMPTS) {
                mainHandler.postDelayed({
                    if (!destroyed) {
                        acknowledgePurchaseIfNeeded(purchase, attempt + 1)
                    }
                }, ACKNOWLEDGEMENT_RETRY_DELAY_MILLIS)
            }
        }
    }

    private fun consumeSupportPurchase(purchase: Purchase, attempt: Int = 1) {
        val params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient.consumeAsync(params) { billingResult, _ ->
            if (destroyed) {
                return@consumeAsync
            }

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                grantPurchase(BillingProductKind.SUPPORT, PurchaseSource.NEW_PURCHASE, null)
                return@consumeAsync
            }

            Log.w(TAG, "Support purchase consumption failed: ${billingResult.debugMessage}")

            if (attempt < MAX_CONSUMPTION_ATTEMPTS) {
                mainHandler.postDelayed({
                    if (!destroyed && purchaseInProgress) {
                        consumeSupportPurchase(purchase, attempt + 1)
                    }
                }, CONSUMPTION_RETRY_DELAY_MILLIS)
            } else {
                failPurchase(billingResult.toBillingError())
            }
        }
    }

    private fun queryKnownPurchase(onResult: (PurchaseQueryResult) -> Unit) {
        if (!billingClient.isReady) {
            onResult(PurchaseQueryResult(RemoveAdsPurchaseStatus.UNAVAILABLE))
            return
        }

        val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (destroyed) {
                return@queryPurchasesAsync
            }

            val eligibleProductIds = launchedProductId?.let { setOf(it) } ?: productIdSet
            onResult(billingResult.toPurchaseQueryResult(purchases, eligibleProductIds, launchedProductId))
        }
    }

    private fun dispatch(action: () -> Unit) {
        if (!destroyed) {
            mainHandler.post(action)
        }
    }

    private enum class PurchaseSource {
        NEW_PURCHASE,
        RESTORE
    }

    private data class PurchaseQueryResult(
            val status: RemoveAdsPurchaseStatus,
            val purchase: Purchase? = null
    )

    private fun List<Purchase>?.findCompletedKnownPurchase(preferredProductId: String?): Purchase? {
        if (preferredProductId != null) {
            return this?.firstOrNull { purchase ->
                preferredProductId in purchase.products && purchase.isCompletedKnownPurchase()
            }
        }

        return this?.firstOrNull { it.isCompletedKnownPurchase() }
    }

    private fun Purchase.isCompletedKnownPurchase(): Boolean {
        return this.products.any { it in productIdSet }
                && this.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    private fun Purchase.kind(): BillingProductKind? {
        return when {
            this.products.any { it in removeAdsProductIdSet } -> BillingProductKind.REMOVE_ADS
            this.products.any { it in supportProductIdSet } -> BillingProductKind.SUPPORT
            else -> null
        }
    }

    private fun BillingResult.toPurchaseQueryResult(
            purchases: List<Purchase>?,
            eligibleProductIds: Set<String>,
            preferredProductId: String? = null
    ): PurchaseQueryResult {
        if (this.responseCode != BillingClient.BillingResponseCode.OK) {
            return PurchaseQueryResult(RemoveAdsPurchaseStatus.UNAVAILABLE)
        }

        val purchase = purchases?.firstOrNull { purchase ->
            preferredProductId != null
                    && preferredProductId in eligibleProductIds
                    && preferredProductId in purchase.products
                    && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        } ?: purchases?.firstOrNull { purchase ->
            purchase.products.any { it in eligibleProductIds }
                    && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        return if (purchase == null) {
            PurchaseQueryResult(RemoveAdsPurchaseStatus.NOT_OWNED)
        } else {
            PurchaseQueryResult(RemoveAdsPurchaseStatus.OWNED, purchase)
        }
    }

    private fun BillingResult.toBillingError(): RemoveAdsBillingError {
        return when (this.responseCode) {
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> RemoveAdsBillingError.FEATURE_NOT_SUPPORTED
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> RemoveAdsBillingError.SERVICE_DISCONNECTED
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> RemoveAdsBillingError.SERVICE_UNAVAILABLE
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> RemoveAdsBillingError.BILLING_UNAVAILABLE
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> RemoveAdsBillingError.PRODUCT_UNAVAILABLE
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> RemoveAdsBillingError.DEVELOPER_ERROR
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> RemoveAdsBillingError.ITEM_ALREADY_OWNED
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> RemoveAdsBillingError.ITEM_NOT_OWNED
            BillingClient.BillingResponseCode.NETWORK_ERROR -> RemoveAdsBillingError.NETWORK_ERROR
            else -> RemoveAdsBillingError.UNKNOWN
        }
    }
}

enum class RemoveAdsBillingError {
    FEATURE_NOT_SUPPORTED,
    SERVICE_DISCONNECTED,
    SERVICE_UNAVAILABLE,
    BILLING_UNAVAILABLE,
    PRODUCT_UNAVAILABLE,
    DEVELOPER_ERROR,
    ITEM_ALREADY_OWNED,
    ITEM_NOT_OWNED,
    NETWORK_ERROR,
    UNKNOWN
}

enum class RemoveAdsPurchaseStatus {
    OWNED,
    NOT_OWNED,
    UNAVAILABLE
}
