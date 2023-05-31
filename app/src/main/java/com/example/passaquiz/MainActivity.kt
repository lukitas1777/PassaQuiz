package com.example.passaquiz

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Math.abs


class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private val demoDataQuestion = arrayListOf<String>()
    private val demoDataAnswer = arrayListOf<String>()
    private lateinit var horizontalScrollView: HorizontalScrollView
    private var totalScore: Int = 0
    private var trueCount: Int = 0
    private var falseCount: Int = 0
    private var passCount: Int = 0
    private var remainingCount: Int = 26

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        val db = Firebase.firestore

        val collectionRef = db.collection("datam")
        val alphabet = ('A'..'Z').toList()

        val selectedEntities = mutableListOf<DocumentSnapshot>()


        for (letter in alphabet) {
            collectionRef.whereEqualTo("letter", letter.toString())
                .get(Source.SERVER)
                .addOnSuccessListener { documents ->
                    val dataList = mutableListOf<DocumentSnapshot>()
                    for (document in documents) {
                        dataList.add(document)
                    }

                    if (dataList.isNotEmpty()) {
                        // Randomly select one entity from the dataList
                        val randomIndex = (0 until dataList.size).random()
                        val randomDocument = dataList[randomIndex]
                        selectedEntities.add(randomDocument)
                    } else {
                        println("No entities found with letter '$letter'")
                    }


                    if (selectedEntities.size == alphabet.size) {
                        selectedEntities.sortBy { it["letter"] as Comparable<Any> }

                        for (entity in selectedEntities) {
                            val data = entity.data
                            // Print or process the data as needed
                            println("Selected Entity: $data")
                            val letter = entity["letter"] as String
                            // Print or process the question as needed
//                            println("Selected letter: $letter")
                            val question = entity["question"] as String
                            demoDataQuestion.add(question)
                            // Print or process the question as needed
//                            println("Selected Question: $question")
                            val answer = entity["answer"] as String
                            demoDataAnswer.add(answer)
                            // Print or process the question as needed
//                            println("Selected answer: $answer")
                        }
                    }
                    println("Demo Data: $demoDataQuestion")
                    viewPager = findViewById<ViewPager2>(R.id.view_pager)
                    viewPager.apply {
                        clipChildren = false
                        clipToPadding = false
                        offscreenPageLimit = 30
                        (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                    }

                    viewPager.adapter = CarouselRVAdapter(demoDataQuestion)


                    val compositePageTransformer = CompositePageTransformer()
                    compositePageTransformer.addTransformer(MarginPageTransformer((40 * Resources.getSystem().displayMetrics.density).toInt()))
                    compositePageTransformer.addTransformer { page, position ->
                        val r = 1 - abs(position)
                        page.scaleY = (0.80f + r * 0.20f)
                    }
                    viewPager.setPageTransformer(compositePageTransformer)
                }
                .addOnFailureListener { exception ->

                }
        }

    }


    fun submitOnClick (v: View?) {
        decideTheResult(v)
    }
    fun changeCurrentQuestion(color: Int) {
        val newPosition = viewPager.currentItem + 1
        viewPager.setCurrentItem(newPosition)
        val currentQuestionButtonId = resources.getIdentifier("button${newPosition}", "id", packageName)
        val currentQuestionButton = findViewById<Button>(currentQuestionButtonId)

        currentQuestionButton.setBackgroundColor(color)
        if (newPosition % 4 == 0) {
            horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView)
            val targetX = horizontalScrollView.scrollX + 740

            horizontalScrollView.smoothScrollTo(
                targetX,
                0
            )

        }
        if (newPosition == 26) {
            val resultValue = "Total Score: ${totalScore.toString()}"
            val correctValue = "Correct: ${trueCount.toString()}, "
            val wrongValue = "False: ${falseCount.toString()}, "
            val passValue = "Pass: ${passCount.toString()}"
            val subTitle = correctValue + wrongValue + passValue
//            val delayMillis = 1000L // 2 seconds
//            Thread.sleep(delayMillis)
            val resultPageIntent = Intent(this, ResultPageActivity::class.java)
            resultPageIntent.putExtra("result", resultValue)
            resultPageIntent.putExtra("subTitle", subTitle)
            startActivity(resultPageIntent)
        }
    }
    fun decideTheResult(v: View?) {
        val clickedCardView = v as CardView?
        val parentView = clickedCardView?.parent as ViewGroup?
        val editText = parentView?.findViewById<EditText>(R.id.edittext)
        val textView = clickedCardView?.findViewById<TextView>(R.id.textView2)

        clickedCardView?.setOnClickListener(null) // Clear the onClick method from card

        if (editText?.text?.isEmpty() == false) {
            if (editText.text.toString().lowercase().trim() == demoDataAnswer[viewPager.currentItem].lowercase().trim()) {
//                private val totalScore: Int = 0
//                private val trueCount: Int = 0
//                private val falseCount: Int = 0
//                private val passCount: Int = 0
//                private val remainingCount: Int = 26
                totalScore += 10
                trueCount += 1
                remainingCount -= 1
                val color = Color.parseColor("#006400")
                textView?.setTextColor(color)
                textView?.text = "Correct! You got 10 points."
                changeCurrentQuestion(color)
            } else {
                totalScore -= 4
                falseCount += 1
                remainingCount -= 1
                val color = Color.parseColor("#de5d2f")
                textView?.setTextColor(color)
                textView?.text = "You got it wrong! -4 points."
                changeCurrentQuestion(color)
            }
        } else {
            totalScore -= 2
            passCount += 1
            remainingCount -= 1
            val color = Color.GRAY

            textView?.setTextColor(color)
            textView?.text = "You passed the question! -2 points."
            changeCurrentQuestion(color)
        }

        textView?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) // Clear the drawableEnd
        editText?.isEnabled = false
        editText?.hint = ""
    }

}