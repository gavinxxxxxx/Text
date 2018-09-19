package me.gavin.app

/**
 * 这里是萌萌哒注释君
 *
 * @author gavin.xiong 2018/9/19.
 */
data class Book(val name: String, val author: String) {

    var cover: String? = null
    var intro: String? = null
    var srcNames: String = ""
}