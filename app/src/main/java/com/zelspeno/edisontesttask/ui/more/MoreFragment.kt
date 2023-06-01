package com.zelspeno.edisontesttask.ui.more

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.zelspeno.edisontesttask.R
import com.zelspeno.edisontesttask.databinding.FragmentMoreBinding
import com.zelspeno.edisontesttask.source.AppsUI
import com.zelspeno.edisontesttask.source.NewsUI
import com.zelspeno.edisontesttask.ui.main.MainViewModel
import com.zelspeno.edisontesttask.utils.ImageGetter
import com.zelspeno.edisontesttask.utils.viewModelCreator

class MoreFragment : Fragment() {

    private val viewModel by viewModelCreator { MainViewModel() }

    private lateinit var binding: FragmentMoreBinding

    private lateinit var news: NewsUI
    private lateinit var game: AppsUI

    private var headerPhoto: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMoreBinding.inflate(inflater, container, false)

        news = this.arguments?.getSerializable("news") as NewsUI
        game = this.arguments?.getSerializable("game") as AppsUI

        binding.moreBackButton.setOnClickListener {
            val bundle =
                Bundle().apply { putSerializable("game", game) }
            viewModel.moveToFragment(view, R.id.navigation_newsFragment, bundle)
        }

        fillUI()

        return binding.root
    }

    /** Init fill User Interface */
    private fun fillUI() {
        with(binding) {
            moreNewsName.text = news.title
            moreDate.text = news.datetime
            moreBody.movementMethod = ScrollingMovementMethod()
        }

        displayHtml(news.body)

        headerPhoto = news.headerPhoto
        Picasso.get().load(headerPhoto).into(binding.moreImage)
    }

    /** Make text([html]) html-tags and html-images sensitive then display it  */
    private fun displayHtml(html: String) {
        val imageGetter = ImageGetter(resources, binding.moreBody)
        val styledText = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
        binding.moreBody.movementMethod = LinkMovementMethod.getInstance()
        binding.moreBody.setLinkTextColor(resources.getColor(R.color.mainTextColorDark, null))
        binding.moreBody.text = styledText
    }
}

