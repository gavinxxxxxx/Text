package me.gavin.app.entity

import android.arch.persistence.room.*
import android.os.Parcelable
import io.reactivex.Flowable
import kotlinx.android.parcel.Parcelize
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL


const val SOURCE_FLAG_NONE = 0 // 正常状态
const val SOURCE_FLAG_DISABLE = 1 // 不可用
const val SOURCE_FLAG_CHECKED = 1 shl 1 // 选中
const val SOURCE_FLAG_TOP = 1 shl 2 // 置顶
const val SOURCE_FLAG_SYS = 1 shl 3 // 内置

@Parcelize
@Entity(tableName = "source")
data class Source(
        @PrimaryKey(autoGenerate = false)
        val url: String,
        val name: String,
        val ruleQueryUrl: String,
        val ruleQueryList: String,
        val ruleQueryName: String,
        val ruleQueryAuthor: String,
        val ruleQueryCover: String?,
        val ruleQueryCategory: String?,
        val ruleQueryIntro: String?,
        val ruleQueryBookUrl: String,
        val ruleBookName: String?,
        val ruleBookAuthor: String?,
        val ruleBookCover: String?,
        val ruleBookCategory: String?,
        val ruleBookState: String?,
        val ruleBookLastTime: String?,
        val ruleBookLastChapter: String?,
        val ruleBookIntro: String?,
        val ruleBookChapterUrl: String,
        val ruleChapterList: String,
        val ruleChapterName: String,
        val ruleChapterContentUrl: String,
        val ruleContent: String,
        val ruleContentFilter: String?,
        val ruleDiscoveryUrl: String?,
        val remark: String?,
        val userAgent: String?,
        val soft: Int = 100,
        var flag: Int = SOURCE_FLAG_NONE) : Parcelable

@Dao
interface SourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(sources: List<Source>)

    @Query("SELECT * FROM source")
    fun listAll(): Flowable<List<Source>>

    @Query("SELECT * FROM source WHERE url = :url")
    fun load(url: String): Flowable<Source>

}


fun Element.list(listRule: String): Elements { // todo list 方法优化
    val result = Elements()
    try {
        val ruleAs = listRule.split('@')
        if (ruleAs.size > 1) {
            this.list(ruleAs[0]).forEach {
                val index = listRule.indexOf('@') + 1
                val listRuleSub = listRule.substring(index)
                result.addAll(it.list(listRuleSub))
            }
        } else {
            val ruleEs = listRule.split('!')
            val rulePs = ruleEs[0].split('.')
            when (rulePs[0]) {
                "children" -> {
                    result.addAll(this.children())
                }
                "text" -> {
                    result.addAll(this.getElementsContainingOwnText(rulePs[1]))
                }
                "id" -> {
                    result.add(this.getElementById(rulePs[1]))
                }
                "class" -> {
                    if (rulePs.size == 2) {
                        result.addAll(this.getElementsByClass(rulePs[1]))
                    } else {
                        result.add(this.getElementsByClass(rulePs[1])[rulePs[2].toInt()])
                    }
                }
                "tag" -> {
                    if (rulePs.size == 2) {
                        result.addAll(this.getElementsByTag(rulePs[1]))
                    } else {
                        result.add(this.getElementsByTag(rulePs[1])[rulePs[2].toInt()])
                    }
                }
            }
            if (ruleEs.size > 1) {
                val ruleCs = ruleEs[1].split(':')
                val ruleCss = ruleCs.mapTo(ArrayList()) {
                    val index = it.toInt()
                    if (index < 0) result.size + index else index
                }
                val filterResult = result.filterIndexed { index, _ ->
                    ruleCss.contains(index)
                }
                result.removeAll(filterResult)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

fun Element.single(singleRule: String?, url: String): String {
    if (singleRule == null || singleRule.isEmpty()) return ""
    try {
        val ruleAs = singleRule.split('@')
        if (ruleAs.size > 1) {
            val index = singleRule.lastIndexOf('@')
            val singleRuleSub = singleRule.substring(0, index)
            this.list(singleRuleSub).forEach {
                val result = it.single(ruleAs.last(), url)
                if (result.isNotEmpty()) return result
            }
        } else {
            return when (singleRule) {
                "text" -> this.text()
                "textNodes" -> this.wholeText() // todo content 过滤
                else -> URL(URL(url), this.attr(singleRule)).toString() // 相对路径转绝对路径
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}
