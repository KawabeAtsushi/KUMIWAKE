package com.pandatone.kumiwake.setting

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.ui.dialogs.CustomDialog
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


/*
実装：https://qiita.com/watanaby0/items/deb60166753533fb00b1
querySkuList() //問い合わせた4つのアイテムの情報を表示する
queryOwned() //購入済みのアイテムを表示する。
startPurchase("android.test.purchased") //「android.test.purchased」を購入するためのダイアログを表示する。
queryPurchaseHistory()
 */


class PurchaseFreeAdOption : AppCompatActivity(), PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {
    private var billingClient: BillingClient? = null
    private var mySkuDetailsList: List<SkuDetails>? = null

    // アプリ開始時に呼ばれる
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // BillingClientを準備する
        billingClient = BillingClient.newBuilder(this)
                .setListener(this).enablePendingPurchases().build()
        if (StatusHolder.adCheck) {
            adStatusCheck()
            StatusHolder.adCheck = false
            finish()
        } else {
            setContentView(R.layout.purchase_layout)
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    val responseCode = billingResult.responseCode
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        querySkuList() //sku初期化から購入処理
                        queryOwned()
                    } else {
                        showResponseCode(responseCode)
                    }
                }

                override fun onBillingServiceDisconnected() { // Try to restart the connection on the next request to
                    finish()
                }
            })
        }
    }

    // アプリ終了時に呼ばれる
    override fun onDestroy() {
        billingClient!!.endConnection()
        super.onDestroy()
    }

    // skuの初期化
    private fun querySkuList() {
        val skuList = ArrayList<String>()
        skuList.add(StatusHolder.ad_free_sku)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient!!.querySkuDetailsAsync(params.build()
        ) { billingResult, skuDetailsList ->
            val responseCode = billingResult.responseCode
            if (responseCode == BillingClient.BillingResponseCode.OK) { // 後の購入手続きのためにSkuの詳細を保持
                mySkuDetailsList = skuDetailsList
                if (StatusHolder.cheakStatus) {
                    StatusHolder.cheakStatus = false
                } else {
                    startPurchase(StatusHolder.ad_free_sku)  //購入
                }
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
    private fun getSkuDetails(sku: String): SkuDetails? {
        var skuDetails: SkuDetails? = null
        if (mySkuDetailsList == null) {
            toastShow("DetailList null")
        } else {
            for (sd in mySkuDetailsList!!) {
                if (sd.sku == sku) skuDetails = sd
            }
            if (skuDetails == null) {
                toastShow("Not match sku")
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
            for (purchase in purchases) { //購入したら呼ばれる
                //ステータスをとれる　val state = handlePurchase(purchase)
                resultStr.append(skuToName(purchase.sku)).append("\n")
                resultStr.append(getString(R.string.purchased))
                deleteAd()
            }
            toastShow(resultStr.toString())
        } else { // Handle error codes.
            showResponseCode(billingResultCode)
        }
    }

    // 購入を承認する
    private fun handlePurchase(purchase: Purchase): String {
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
        StatusHolder.adCheck = true
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            showResponseCode(responseCode)
        }
    }

    // 購入済みアイテムを問い合わせる（キャッシュ処理）
    private fun queryOwned() {
        val history = findViewById<TextView>(R.id.purchsed_history)
        val purchasesResult = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
        val responseCode = purchasesResult.responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            val purchases = purchasesResult.purchasesList
            if (purchases.isEmpty()) {
                history.text = getString(R.string.nothing)
            } else {
                for (purchase in purchases) {
                    history.text = (skuToName(purchase.sku) + "\n")
                }
            }
        } else {
            showResponseCode(responseCode)
        }
    }

    //広告削除は購入済みか？
    private fun adStatusCheck() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val responseCodeB = billingResult.responseCode
                if (responseCodeB == BillingClient.BillingResponseCode.OK) {
                    val purchasesResult = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
                    val responseCodeP = purchasesResult.responseCode
                    if (responseCodeP == BillingClient.BillingResponseCode.OK) {
                        val purchases = purchasesResult.purchasesList
                        for (purchase in purchases) {
                            if (purchase.sku == StatusHolder.ad_free_sku) {
                                deleteAd()
                            } //広告削除済みか判定
                        }
                    } else {
                        showResponseCode(responseCodeP)
                    }
                } else {
                    showResponseCode(responseCodeB)
                }
            }

            override fun onBillingServiceDisconnected() { // Try to restart the connection on the next request to
            }
        })
    }

    //skuを商品名に変換
    private fun skuToName(sku: String):String{
        when(sku){
            StatusHolder.ad_free_sku -> return getString(R.string.ad_delete)
        }
        return getString(R.string.nothing)
    }

    // サーバの応答を表示する
    fun showResponseCode(responseCode: Int) {
        when (responseCode) {
            BillingClient.BillingResponseCode.USER_CANCELED -> toastShow(getString(R.string.cancel_purchase))
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> toastShow("SERVICE_UNAVAILABLE")
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> toastShow("BILLING_UNAVAILABLE")
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> toastShow("ITEM_UNAVAILABLE")
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> toastShow("DEVELOPER_ERROR")
            BillingClient.BillingResponseCode.ERROR -> toastShow("ERROR")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> toastShow(getString(R.string.ad_deleted_already))
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> toastShow("ITEM_NOT_OWNED")
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> toastShow("SERVICE_DISCONNECTED")
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> toastShow("FEATURE_NOT_SUPPORTED")
        }
        finish()
    }

    //トースト表示
    private fun toastShow(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    //広告非表示処理
    private fun deleteAd(){
        StatusHolder.adDeleated = true
        MainActivity.mAdView.visibility = View.GONE
    }
}