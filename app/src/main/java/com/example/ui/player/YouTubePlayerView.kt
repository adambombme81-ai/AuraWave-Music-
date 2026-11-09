package com.example.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.player.UnifiedPlayerManager

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerView(
    videoId: String,
    playerManager: UnifiedPlayerManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webView = remember {
        createYouTubeWebView(context, videoId, playerManager)
    }

    DisposableEffect(videoId) {
        if (videoId.isNotBlank()) {
            webView.evaluateJavascript("loadVideo('$videoId');", null)
        }
        onDispose { }
    }

    DisposableEffect(Unit) {
        playerManager.onYouTubeCommand = { action, arg ->
            when (action) {
                "load" -> webView.evaluateJavascript("loadVideo('$arg');", null)
                "play" -> webView.evaluateJavascript("playVideo();", null)
                "pause" -> webView.evaluateJavascript("pauseVideo();", null)
                "seek" -> webView.evaluateJavascript("seekTo($arg);", null)
            }
        }
        onDispose {
            playerManager.onYouTubeCommand = null
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun createYouTubeWebView(
    context: Context,
    initialVideoId: String,
    playerManager: UnifiedPlayerManager
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(Color.BLACK)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        webViewClient = WebViewClient()
        webChromeClient = WebChromeClient()

        addJavascriptInterface(
            object {
                @JavascriptInterface
                fun onTimeUpdate(currentTime: Float, duration: Float, isPlaying: Boolean) {
                    playerManager.updateFromExternalPlayer(currentTime, duration, isPlaying)
                }

                @JavascriptInterface
                fun onStateChange(state: Int) {
                    val isPlaying = (state == 1) // 1 = playing, 2 = paused
                    playerManager.updateFromExternalPlayer(
                        playerManager.currentPositionMs.value / 1000f,
                        playerManager.durationMs.value / 1000f,
                        isPlaying
                    )
                }
            },
            "AndroidBridge"
        )

        val html = getYouTubeHtml(initialVideoId)
        loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
    }
}

private fun getYouTubeHtml(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            body, html { margin:0; padding:0; width:100%; height:100%; background:#090810; overflow:hidden; }
            #player { width:100%; height:100%; }
          </style>
        </head>
        <body>
          <div id="player"></div>
          <script>
            var tag = document.createElement('script');
            tag.src = "https://www.youtube.com/iframe_api";
            var firstScriptTag = document.getElementsByTagName('script')[0];
            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

            var player;
            function onYouTubeIframeAPIReady() {
              player = new YT.Player('player', {
                videoId: '$videoId',
                playerVars: {
                  'autoplay': 1,
                  'playsinline': 1,
                  'controls': 1,
                  'rel': 0,
                  'modestbranding': 1,
                  'fs': 0
                },
                events: {
                  'onReady': onPlayerReady,
                  'onStateChange': onPlayerStateChange
                }
              });
            }
            function onPlayerReady(event) {
              event.target.playVideo();
              setInterval(function() {
                if (player && player.getCurrentTime) {
                  try {
                    var ct = player.getCurrentTime() || 0;
                    var dur = player.getDuration() || 0;
                    var st = player.getPlayerState();
                    if (window.AndroidBridge) {
                      window.AndroidBridge.onTimeUpdate(ct, dur, st === 1);
                    }
                  } catch(e) {}
                }
              }, 500);
            }
            function onPlayerStateChange(event) {
              if (window.AndroidBridge) {
                window.AndroidBridge.onStateChange(event.data);
              }
            }
            function playVideo() { if (player && player.playVideo) player.playVideo(); }
            function pauseVideo() { if (player && player.pauseVideo) player.pauseVideo(); }
            function seekTo(sec) { if (player && player.seekTo) player.seekTo(sec, true); }
            function loadVideo(id) { if (player && player.loadVideoById) player.loadVideoById(id); }
          </script>
        </body>
        </html>
    """.trimIndent()
}
