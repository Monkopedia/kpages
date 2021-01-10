package com.monkopedia.kpages.demo

import com.monkopedia.dynamiclayout.Gravity
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.kpages.navigator
import com.monkopedia.lanterna.Lanterna
import com.monkopedia.lanterna.ThemeData
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.buildViews
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.runGuiThread
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) = Demo().run()

class Demo {
    fun run() {
        Lanterna.init(
            File("build/fonts"),
            Json.decodeFromString(ThemeData.serializer(), defaultThemeText),
            Properties().apply {
                load(defaultFontText.byteInputStream())
            }
        )
        val navigation = Navigation(Lanterna.gui)
        runBlocking {
            val app = DemoApp()
            println("Open screen!")
            var title: SpannableLabel
            navigation.header = buildViews {
                border {
                    vertical {
                        title = label("HEADER!!").layoutParams(Wrap, Wrap, Gravity.CENTER)
                    }
                }
            }.first()
            val navigator = app.navigator(navigation)
            navigator.title.attach {
                title.setText(it)
            }
            navigator.push("/")
            println("Done screen!")

            runGuiThread(true)
            LoggerFactory.getLogger(Demo::class.java).debug("Shutting down main thread")
            navigation.destroy()
            Lanterna.screen.close()
            Lanterna.terminal.close()
            exitProcess(1)
        }
    }
}

private const val defaultFontText = """
h1=http://www.figlet.org/fonts/starwars.flf
h2=http://www.figlet.org/fonts/straight.flf
"""

private const val defaultThemeText =
    """
{
  "colors": {
    "primary": "#81c784",
    "secondary": "#b2fab4",
    "tertiary": "#519657",
    "text": "#fafafa",
    "disabledText": "#fafafa",
    "bg": "#282828",
    "bgHighlight": "#363636",
    "disabled": "#969696",
    "blackeled": "#000000"
  },
  "default": {
    "normal": {
      "foreground": "text",
      "background": "black",
      "sgrs": []
    },
    "selected": {
      "foreground": "bg",
      "background": "primary",
      "sgrs": ["BOLD"]
    },
    "active": {
      "foreground": "bg",
      "background": "primary",
      "sgrs": ["BOLD"]
    },
    "prelight": {
      "foreground": "primary",
      "background": "black",
      "sgrs": []
    },
    "insensitive": {
      "foreground": "disabledText",
      "background": "black",
      "sgrs": []
    }
  },
  "postRenderer": null,
  "themeOverrides": {
    "com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer": {
      "prelight": {
        "foreground": "text",
        "background": "black"
      }
    },
    "mdview.Style": {
      "custom": {
        "LINK": {
          "foreground": "primary",
          "background": "bg",
          "sgrs": ["BOLD"]
        },
        "LINK_SELECTED": {
          "foreground": "bg",
          "background": "primary",
          "sgrs": ["BOLD"]
        },
        "CODE_BLOCK": {
          "foreground": "text",
          "background": "bg",
          "sgrs": []
        },
        "HEADER_PRIMARY": {
          "foreground": "text",
          "background": "black",
          "sgrs": ["UNDERLINE", "BOLD"]
        },
        "HEADER_SECONDARY": {
          "foreground": "text",
          "background": "black",
          "sgrs": ["BOLD"]
        }
      },
      "properties": {
        "ASCII_FONTS": true
      }
    }
  }
}

"""
