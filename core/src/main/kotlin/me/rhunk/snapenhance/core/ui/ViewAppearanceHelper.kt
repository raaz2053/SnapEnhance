package me.rhunk.snapenhance.core.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.Shape
import android.view.Gravity
import android.view.View
import android.widget.Switch
import android.widget.TextView
import me.rhunk.snapenhance.common.Constants
import kotlin.random.Random

fun View.applyTheme(componentWidth: Int? = null, hasRadius: Boolean = false, isAmoled: Boolean = true) {
    ViewAppearanceHelper.applyTheme(this, componentWidth, hasRadius, isAmoled)
}

private val foregroundDrawableListTag = Random.nextInt(0x7000000, 0x7FFFFFFF)

@Suppress("UNCHECKED_CAST")
private fun View.getForegroundDrawables(): MutableMap<String, Drawable> {
    return getTag(foregroundDrawableListTag) as? MutableMap<String, Drawable>
        ?: mutableMapOf<String, Drawable>().also {
        setTag(foregroundDrawableListTag, it)
    }
}

private fun View.updateForegroundDrawable() {
    foreground = ShapeDrawable(object: Shape() {
        override fun draw(canvas: Canvas, paint: Paint) {
            getForegroundDrawables().forEach { (_, drawable) ->
                drawable.draw(canvas)
            }
        }
    })
}

fun View.removeForegroundDrawable(tag: String) {
    getForegroundDrawables().remove(tag)?.let {
        updateForegroundDrawable()
    }
}

fun View.addForegroundDrawable(tag: String, drawable: Drawable) {
    getForegroundDrawables()[tag] = drawable
    updateForegroundDrawable()
}


object ViewAppearanceHelper {
    @SuppressLint("UseSwitchCompatOrMaterialCode", "RtlHardcoded", "DiscouragedApi",
        "ClickableViewAccessibility"
    )
    private var sigColorTextPrimary: Int = 0
    private var sigColorBackgroundSurface: Int = 0

    private fun createRoundedBackground(color: Int, hasRadius: Boolean): Drawable {
        if (!hasRadius) return ColorDrawable(color)
        //FIXME: hardcoded radius
        return ShapeDrawable().apply {
            paint.color = color
            shape = android.graphics.drawable.shapes.RoundRectShape(
                floatArrayOf(20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f),
                null,
                null
            )
        }
    }

    @SuppressLint("DiscouragedApi")
    fun applyTheme(component: View, componentWidth: Int? = null, hasRadius: Boolean = false, isAmoled: Boolean = true) {
        val resources = component.context.resources
        if (sigColorBackgroundSurface == 0 || sigColorTextPrimary == 0) {
            with(component.context.theme) {
                sigColorTextPrimary = obtainStyledAttributes(
                    intArrayOf(resources.getIdentifier("sigColorTextPrimary", "attr", Constants.SNAPCHAT_PACKAGE_NAME))
                ).getColor(0, 0)

                sigColorBackgroundSurface = obtainStyledAttributes(
                    intArrayOf(resources.getIdentifier("sigColorBackgroundSurface", "attr", Constants.SNAPCHAT_PACKAGE_NAME))
                ).getColor(0, 0)
            }
        }

        val snapchatFontResId = resources.getIdentifier("avenir_next_medium", "font", Constants.SNAPCHAT_PACKAGE_NAME)
        val scalingFactor = resources.displayMetrics.densityDpi.toDouble() / 400

        with(component) {
            if (this is TextView) {
                setTextColor(sigColorTextPrimary)
                setShadowLayer(0F, 0F, 0F, 0)
                gravity = Gravity.CENTER_VERTICAL
                componentWidth?.let { width = it}
                height = (150 * scalingFactor).toInt()
                isAllCaps = false
                textSize = 16f
                typeface = resources.getFont(snapchatFontResId)
                outlineProvider = null
                setPadding((40 * scalingFactor).toInt(), 0, (40 * scalingFactor).toInt(), 0)
            }
            if (isAmoled) {
                background = StateListDrawable().apply {
                    addState(intArrayOf(), createRoundedBackground(color = sigColorBackgroundSurface, hasRadius))
                    addState(intArrayOf(android.R.attr.state_pressed), createRoundedBackground(color = 0x5395026, hasRadius))
                }
            } else {
                setBackgroundColor(0x0)
            }
        }

        if (component is Switch) {
            with(resources) {
                component.switchMinWidth = getDimension(getIdentifier("v11_switch_min_width", "dimen", Constants.SNAPCHAT_PACKAGE_NAME)).toInt()
            }
            component.trackTintList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)
                ), intArrayOf(
                    Color.parseColor("#1d1d1d"),
                    Color.parseColor("#26bd49")
                )
            )
            component.thumbTintList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)
                ), intArrayOf(
                    Color.parseColor("#F5F5F5"),
                    Color.parseColor("#26bd49")
                )
            )
        }
    }

    fun newAlertDialogBuilder(context: Context?) = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
}