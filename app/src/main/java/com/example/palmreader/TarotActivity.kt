package com.example.palmreader

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.palmreader.databinding.ActivityTarotBinding

class TarotActivity : AppCompatActivity() {
    private lateinit var b: ActivityTarotBinding
    private val topics = TarotTopic.entries

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTarotBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.spTopic.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, topics.map { it.vi })
        b.btnShuffle.setOnClickListener { deal() }
        deal()
    }

    /** Xào cả bộ 78 lá rồi bày 12 lá úp. */
    private fun deal() {
        b.cardResult.visibility = View.GONE
        b.grid.removeAllViews()
        TAROT_DECK.shuffled().take(12).forEach { card ->
            val btn = Button(this).apply {
                text = "🂠"
                textSize = 32f
                contentDescription = "Lá bài úp"
                layoutParams = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f)).apply { width = 0 }
                setOnClickListener { reveal(card) }
            }
            b.grid.addView(btn)
        }
    }

    private fun reveal(card: TarotCard) {
        val topic = topics[b.spTopic.selectedItemPosition]
        for (i in 0 until b.grid.childCount) b.grid.getChildAt(i).isEnabled = false
        b.tvCardName.text = card.name
        b.tvCardMeaning.text = card.meaning
        b.tvCardTopic.text = tarotReading(card, topic)
        b.cardResult.visibility = View.VISIBLE
        b.root.post { b.root.smoothScrollTo(0, b.cardResult.top) }
    }
}
