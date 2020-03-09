package com.pandatone.kumiwake.setting

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.android.billingclient.api.*
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PurchaseFreeAdOption(private val context: Context) {

    fun deleteAd(activity: Activity,adView: AdView){
        val billingRepository = PurchaseFreeAdOption(context)
        // アプリケーション開始直後のMainActivityで支払い情報を確認してViewを初期化してみる
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) { billingRepository.startConnection(this) }
            val billingModel = withContext(Dispatchers.Default) {
                if (billingRepository.connected) {
                    billingRepository.queryPurchaseHistoryAsync()
                } else {
                    return@withContext null
                }
            }
            withContext(Dispatchers.Main) {
                billingModel?.let {
                    if (it.paidAdFree) {
                        adView.visibility = View.GONE
                    }
                }
            }

            // 広告非表示オプションの支払いダイアログを出してみる
            billingRepository.showBillingDialog(Skus.AdFree, this, activity)
        }
    }

    var purchasesUpdatedListeners: MutableList<(BillingModel) -> Unit> = mutableListOf()

    private var billingClient: BillingClient =
            BillingClient.newBuilder(this.context).enablePendingPurchases().setListener { billingResult, purchases ->
                val model = this@PurchaseFreeAdOption.purchaseListToBillingModel(purchases ?: return@setListener)
                this@PurchaseFreeAdOption.purchasesUpdatedListeners.forEach {
                    it(model)
                }
            }.build()

    var connected = false

    suspend fun startConnection(scope: CoroutineScope) = suspendCoroutine<Unit> { continuation ->
        scope.launch(Dispatchers.IO) {
            this@PurchaseFreeAdOption.billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        this@PurchaseFreeAdOption.connected = true
                    }
                    continuation.resume(Unit)
                }

                override fun onBillingServiceDisconnected() {
                    this@PurchaseFreeAdOption.connected = false
                }
            })
        }
    }

    private suspend fun querySku(skus: List<String>) = suspendCoroutine<List<SkuDetails>?> { continuation ->
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skus).setType(BillingClient.SkuType.INAPP)
        this.billingClient.querySkuDetailsAsync(params.build()) { _, skuDetailsList ->
            continuation.resume(skuDetailsList)
        }
    }

    // queryPurchaseHistoryAsyncをラップしているので名前を継承しているが、
    // Kotlinの文脈で言うDeferred<T>を戻り値とするメソッドと区別できないので変えたほうが良さそう
    suspend fun queryPurchaseHistoryAsync() = suspendCoroutine<BillingModel> { continuation ->
        this.billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { billingResult, purchaseHistoryRecordList ->
            continuation.resume(purchaseHistoryRecordList.toBillingModel())
        }
    }

    // Activityを扱う関係上、Repositoryの概念から少し外れそうなので別の何かに分離すべきかも
    fun showBillingDialog(sku: Skus, scope: CoroutineScope, activity: Activity) {
        scope.launch(Dispatchers.IO) {
            val skuDetails = runBlocking { this@PurchaseFreeAdOption.querySku(listOf(sku.id)) } ?: return@launch
            if (skuDetails.isEmpty()) return@launch
            val params = BillingFlowParams.newBuilder().setSkuDetails(skuDetails.first()).build()
            withContext(Dispatchers.Main) {
                this@PurchaseFreeAdOption.billingClient.launchBillingFlow(activity, params)
            }
        }
    }

    // こちらもExtentionで実装したかったが、以下List<PurchaseHistoryRecord>.toBillingModel()が存在する関係上、
    // こちらは普通のメソッドとして実装
    private fun purchaseListToBillingModel(list: List<Purchase>): BillingModel {
        var paidAdFree = false
        list.forEach {
            if (it.sku == Skus.AdFree.id) {
                paidAdFree = true
            }
        }
        return BillingModel(
                paidAdFree = paidAdFree
        )
    }

    // 一度買ったきり、消耗しないもの(一回払えばずっと適用されるプレミアムプランなど)にはこれで問題なさそう
    // 消耗するものについてはこれで問題ないか未確認
    private fun List<PurchaseHistoryRecord>.toBillingModel(): BillingModel {
        var paidAdFree = false
        this.forEach {
            if (it.sku == Skus.AdFree.id) {
                paidAdFree = true
            }
        }
        return BillingModel(
                paidAdFree = paidAdFree
        )
    }
}

data class BillingModel(
        val paidAdFree: Boolean
)

enum class Skus(val id: String) {
    AdFree("ad_free")
}