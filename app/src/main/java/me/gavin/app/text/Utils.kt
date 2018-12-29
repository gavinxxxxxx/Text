package me.gavin.app.text

import me.gavin.app.entity.Line
import me.gavin.app.entity.Word
import java.util.*
import java.util.regex.Pattern

object Utils {

    fun breakText(remaining: String, lineIndent: Boolean): Int {
        val count = Config.textPaint.breakText(remaining, true, Config.width
                - Config.leftPadding - Config.rightPadding - if (lineIndent) Config.indent else 0F, null)
        return if (count >= remaining.length) count else countReset(remaining, count, lineIndent)
    }

    /**
     * 分行衔接调整 - 标点 & 单词 - 只考虑标准情况
     *
     * @return count 数量 负数代表要加 -
     */
    private fun countReset(remaining: String, countz: Int, lineIndent: Boolean): Int {
        var count = countz
        if (Config.REGEX_PUNCTUATION_N.toRegex().matches(remaining.substring(count, count + 1))) { // 下一行第一个是标点
            if (!"${Config.REGEX_PUNCTUATION_N}*".toRegex().matches(remaining.substring(count - 2, count))) { // 当前行末两不都是标点 - 可退一个字符去下一行
                count -= 1
                return countReset(remaining, count, lineIndent)
            }
        } else if (Config.REGEX_WORD.toRegex().matches(remaining.substring(count - 1, count + 1))) { // 上一行最后一个字符和下一行第一个字符符合单词形式
            val line = remaining.substring(0, count)
            val matcher = Pattern.compile(Config.REGEX_CHARACTER).matcher(line)
            var groupCount = 0
            var group = ""
            while (matcher.find()) {
                groupCount++
                group = matcher.group()
            }
            val indent = if (lineIndent) Config.indent else 0F
            val lineWidth = Config.width - Config.leftPadding - Config.rightPadding - indent
            val end = line.lastIndexOf(group)
            val textWidth = Config.textPaint.measureText(line.substring(0, end))
            val extraSpace = lineWidth - textWidth // 剩余空间
            val spacing = extraSpace / Math.max(1, groupCount - 1)
            return if (spacing <= Config.wordSpacingSlop) end else 1 - count // 单词间距过大 - 改为 abc- 形式
        }
        return count
    }

    fun line2Word(lines: List<Line>) = LinkedList<Word>().apply {
        for (line in lines) {
            val indent = if (line.lineIndent) Config.indent else 0F
            if (!line.lineAlign || line.text.length <= 1) { // 不需要分散对齐 | 只有一个字符
                this@apply.add(Word(line.text, Config.leftPadding + indent, line.y.toFloat()))
                continue
            }

            val textWidth = Config.textPaint.measureText(line.text)
            val lineWidth = Config.width - Config.leftPadding - Config.rightPadding - indent
            val extraSpace = lineWidth - textWidth // 剩余空间
            if (extraSpace <= 10) { // 没有多余空间 - 不需要分散对齐
                this@apply.add(Word(line.text, Config.leftPadding + indent, line.y.toFloat()))
                continue
            }

            val matcher = Pattern.compile(Config.REGEX_CHARACTER).matcher(line.text)
            var wordCount = 0
            while (matcher.find()) {
                wordCount++
            }
            if (wordCount > 1) { // 多个单词 - 词间距
                val workSpacing = extraSpace / (wordCount - 1)
                val startX = Config.leftPadding + indent
                var x: Float
                val sb = StringBuilder()
                var spacingCount = 0
                matcher.reset()
                while (matcher.find()) {
                    val word = matcher.group()
                    x = startX + Config.textPaint.measureText(sb.toString()) + workSpacing * spacingCount
                    this@apply.add(Word(word, x, line.y.toFloat()))
                    sb.append(word)
                    spacingCount++
                }
            } else { // 单个单词 - 字间距
                val workSpacing = extraSpace / (line.text.length - 1)
                val startX = Config.leftPadding + indent
                var x: Float
                for (i in 0 until line.text.length) {
                    val word = line.text[i].toString()
                    x = startX + Config.textPaint.measureText(line.text.substring(0, i)) + workSpacing * i
                    this@apply.add(Word(word, x, line.y.toFloat()))
                }
            }
        }
    }

}