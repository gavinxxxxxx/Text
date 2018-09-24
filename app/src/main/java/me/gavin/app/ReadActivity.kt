package me.gavin.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.gavin.R
import me.gavin.base.BaseActivity
import me.gavin.databinding.ActivityReadBinding


class ReadActivity : BaseActivity<ActivityReadBinding>() {

    private lateinit var mBook: Book

    companion object {
        fun start(context: Context, book: Book) {
            context.startActivity(Intent(context, ReadActivity::class.java)
                    .putExtra("book", book))
        }
    }

    override fun getLayoutId() = R.layout.activity_read

    override fun afterCreate(savedInstanceState: Bundle?) {
        mBook = intent.getParcelableExtra("book") ?: return

    }

}