package com.example.luyin

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val webView = findViewById<WebView>(R.id.webView)
        setupWebView(webView)
    }

    private fun setupWebView(webView: WebView) {
        // 基础配置
        webView.settings.apply {
            javaScriptEnabled = true // 如需支持JS
            domStorageEnabled = true // 支持本地存储
            loadsImagesAutomatically = true // 自动加载图片
        }

        // 加载本地HTML文件
        val htmlPath = "file:///android_asset/html/user_agreement.html"
        webView.loadUrl(htmlPath)

        // 处理页面加载错误
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Toast.makeText(
                    this@WebViewActivity,
                    "加载协议失败：$description",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
