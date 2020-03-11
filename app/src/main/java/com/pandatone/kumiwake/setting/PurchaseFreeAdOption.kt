package com.pandatone.kumiwake.setting

import android.R
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*


/*
querySkuList() //問い合わせた4つのアイテムの情報を表示する
queryOwned() //購入済みのアイテムを表示する。
startPurchase("android.test.purchased") //「android.test.purchased」を購入するためのダイアログを表示する。
queryPurchaseHistory()
 */


class PurchaseFreeAdOption() : AppCompatActivity() , PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {
    var textView1: TextView? = null
    private var billingClient: BillingClient? = null
    var mySkuDetailsList: List<SkuDetails>? = null

    // アプリ開始時に呼ばれる
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // BillingClientを準備する
        billingClient = BillingClient.newBuilder(this)
                .setListener(this).enablePendingPurchases().build()
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val responseCode = billingResult.responseCode
                if (responseCode == BillingClient.BillingResponseCode.OK) { // The BillingClient is ready. You can query purchases here.
                    textView1!!.text = "Billing Setup OK"
                    startPurchase("android.test.purchased")
                } else {
                    showResponseCode(responseCode)
                }
            }

            override fun onBillingServiceDisconnected() { // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
                textView1!!.text = "Billing Servise Disconnected. Retry"
            }
        })
    }

    // アプリ終了時に呼ばれる
    override fun onDestroy() {
        billingClient!!.endConnection()
        super.onDestroy()
    }

    // 購入したいアイテムを問い合わせる
    private fun querySkuList() {
        val skuList = ArrayList<String>()
        skuList.add("android.test.purchased") // prepared by Google
        skuList.add("android.test.canceled")
        skuList.add("android.test.refunded")
        skuList.add("android.test.item_unavailable")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient!!.querySkuDetailsAsync(params.build()
        ) { billingResult, skuDetailsList ->
            // Process the result.
            val resultStr = StringBuffer("")
            val responseCode = billingResult.responseCode
            if (responseCode == BillingClient.BillingResponseCode.OK) { // 後の購入手続きのためにSkuの詳細を保持
                mySkuDetailsList = skuDetailsList
                // リストを表示
                if (skuDetailsList != null) {
                    for (item in skuDetailsList) {
                        val skuDetails = item as SkuDetails
                        val sku = skuDetails.sku
                        val price = skuDetails.price
                        resultStr.append("Sku=$sku Price=$price\n")
                    }
                } else {
                    resultStr.append("No Sku")
                }
                textView1!!.text = resultStr
            } else {
                showResponseCode(responseCode)
            }
        }
    }

    // 購入処理を開始する
    private fun startPurchase(sku: String) {
        val skuDetails = getSkuDetails(sku)
        if (skuDetails != null) {
            val params = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()
            val billingResult = billingClient!!.launchBillingFlow(this, params)
            showResponseCode(billingResult.responseCode)
        }
    }

    // 指定したSKUの詳細をリスト内から得る
    fun getSkuDetails(sku: String): SkuDetails? {
        var skuDetails: SkuDetails? = null
        if (mySkuDetailsList == null) {
            textView1!!.text = "Exec [Get Skus] first"
        } else {
            for (sd in mySkuDetailsList!!) {
                if (sd.sku == sku) skuDetails = sd
            }
            if (skuDetails == null) {
                textView1!!.text = "$sku is not found"
            }
        }
        return skuDetails
    }

    // 購入結果の更新時に呼ばれる
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val resultStr = StringBuffer("")
        val billingResultCode = billingResult.responseCode
        if (billingResultCode == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (purchase in purchases) { //購入を承認する
                val state = handlePurchase(purchase)
                //購入したSkuの文字列と承認結果を表示する
                val sku = purchase.sku
                resultStr.append(sku).append("\n")
                resultStr.append(" State=").append(state).append("\n")
            }
            textView1!!.text = resultStr
        } else { // Handle error codes.
            showResponseCode(billingResultCode)
        }
    }

    // 購入を承認する
    fun handlePurchase(purchase: Purchase): String {
        var stateStr = "error"
        val purchaseState = purchase.purchaseState
        if (purchaseState == Purchase.PurchaseState.PURCHASED) { // Grant entitlement to the user.
            stateStr = "purchased"
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams, this)
            }
        } else if (purchaseState == Purchase.PurchaseState.PENDING) {
            stateStr = "pending"
        } else if (purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            stateStr = "unspecified state"
        }
        return stateStr
    }

    // 購入承認の結果が戻る
    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            showResponseCode(responseCode)
        }
    }

    // 購入済みアイテムを問い合わせる（キャッシュ処理）
    fun queryOwned() {
        val resultStr = StringBuffer("")
        val purchasesResult = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
        val responseCode = purchasesResult.responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            resultStr.append("Query Success\n")
            val purchases = purchasesResult.purchasesList
            if (purchases.isEmpty()) {
                resultStr.append("Owned Nothing")
            } else {
                for (purchase in purchases) {
                    resultStr.append(purchase.sku).append("\n")
                }
            }
            textView1!!.text = resultStr
        } else {
            showResponseCode(responseCode)
        }
    }

    // 購入履歴を問い合わせる（ネットワークアクセス処理）
    fun queryPurchaseHistory() {
        billingClient!!.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP
        ) { billingResult, purchasesList ->
            val responseCode = billingResult.responseCode
            if (responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchasesList == null || purchasesList.size == 0) {
                    textView1!!.text = "No History"
                } else {
                    for (purchase in purchasesList) { // Process the result.
                        textView1!!.text = ("Purchase History="
                                + purchase.toString() + "\n")
                    }
                }
            } else {
                showResponseCode(responseCode)
            }
        }
    }

    // サーバの応答を表示する
    fun showResponseCode(responseCode: Int) {
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> textView1!!.text = "OK"
            BillingClient.BillingResponseCode.USER_CANCELED -> textView1!!.text = "USER_CANCELED"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> textView1!!.text = "SERVICE_UNAVAILABLE"
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> textView1!!.text = "BILLING_UNAVAILABLE"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> textView1!!.text = "ITEM_UNAVAILABLE"
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> textView1!!.text = "DEVELOPER_ERROR"
            BillingClient.BillingResponseCode.ERROR -> textView1!!.text = "ERROR"
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> textView1!!.text = "ITEM_ALREADY_OWNED"
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> textView1!!.text = "ITEM_NOT_OWNED"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> textView1!!.text = "SERVICE_DISCONNECTED"
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> textView1!!.text = "FEATURE_NOT_SUPPORTED"
        }
    }
}