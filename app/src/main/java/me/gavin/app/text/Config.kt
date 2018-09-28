package me.gavin.app.text

import android.graphics.Paint
import android.view.ViewConfiguration
import me.gavin.base.App
import me.gavin.util.DisplayUtil
import me.gavin.util.SPUtil


object Config {

    var width: Int = 0
    var height: Int = 0

    var pagePreCount = 0
    var segmentPreCount = 0

    var textSize: Int = 0 // 文字大小
    var textAscent: Float = 0F
    var textDescent: Float = 0F
    var textLeading: Float = 0F
    var textTop: Int = 0
    var textBottom: Int = 0
    var textHeight: Int = 0 // 文字高度
    var textColor: Int = 0 // 文字颜色
    var bgColor: Int = 0

    var topPadding: Int = 0
    var bottomPadding: Int = 0
    var leftPadding: Int = 0
    var rightPadding: Int = 0

    var segmentSpacing: Int = 0 // 段间距
    var lineSpacing: Int = 0 // 行间距
    var indent: Float = 0F // 首行缩进
    var lineAlignSlop: Float = 0F // 行分散对齐临界值
    var wordSpacingSlop: Float = 0F // 单词最大间距

    val textPaint: Paint
    val bgPaint: Paint
    val debugPaint: Paint

    var pageCount: Int = 0 // 页面数量
    var pageElevation: Int = 0 // 页高度
    var touchSlop: Int = 0 // 滑动临界值
    var flingVelocity: Int = 0 // 抛投临界值
    var flipAnimDuration: Float = 0.toFloat() // 翻页动画时长比例

    init {
        textSize = SPUtil.getInt("textSize", 40)
        textColor = SPUtil.getInt("textColor", 0xFFA9B7C6.toInt())
        bgColor = SPUtil.getInt("bgColor", 0xFF252525.toInt())

        topPadding = SPUtil.getInt("topPadding", 80)
        bottomPadding = SPUtil.getInt("bottomPadding", 50)
        leftPadding = SPUtil.getInt("leftPadding", 50)
        rightPadding = SPUtil.getInt("rightPadding", leftPadding)

        segmentSpacing = SPUtil.getInt("segmentSpacing", textSize)
        lineSpacing = SPUtil.getInt("lineSpacing", textSize / 2)
        indent = SPUtil.getFloat("indent", textSize * 2f)
        lineAlignSlop = 10f
        wordSpacingSlop = textSize * 0.5f

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize.toFloat()
        textPaint.color = textColor

        bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.color = bgColor

        debugPaint = Paint()
        debugPaint.color = 0x22222222

        val fontMetrics = textPaint.fontMetrics
        textAscent = fontMetrics.ascent // textAscent = textSize * -0.9277344f;
        textDescent = fontMetrics.descent // textDescent = textSize * 0.24414062f;
        textLeading = fontMetrics.leading
        textTop = Math.ceil(fontMetrics.top.toDouble()).toInt() // -1.5 -> -1
        textBottom = Math.floor(fontMetrics.bottom.toDouble()).toInt() // 1.5 -> 1
        textHeight = textBottom - textTop // textHeight = textSize * 1.3271484f;

        pageCount = 3
        pageElevation = DisplayUtil.dp2px(10f)
        touchSlop = ViewConfiguration.get(App.app).scaledTouchSlop
        flingVelocity = ViewConfiguration.get(App.app).scaledMinimumFlingVelocity * 2
        flipAnimDuration = 0.4f
    }

    /**
     * 尺寸变化时重新计算预加载数量
     */
    fun syncSize(w: Int, h: Int) {
        width = w
        height = h
        segmentPreCount = Math.ceil(((width - leftPadding - rightPadding) / getLetterMinWidth()).toDouble()).toInt() / 2
        pagePreCount = segmentPreCount * Math.ceil(((height - topPadding - bottomPadding) / textHeight).toDouble()).toInt()
    }

    /**
     * 获取字母最小宽度 todo 预加载的数量过多 改成不够在取？
     */
    private fun getLetterMinWidth(): Float {
        return arrayOf("f", "i", "j", "l", "r", "1")
                .map { textPaint.measureText(it) }
                .min()!!
    }
}