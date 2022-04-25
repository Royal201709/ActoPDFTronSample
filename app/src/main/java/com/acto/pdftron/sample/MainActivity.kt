package com.acto.pdftron.sample

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.acto.android.databinding.ActivityMainBinding
import com.pdftron.common.PDFNetException
import com.pdftron.pdf.Annot
import com.pdftron.pdf.Bookmark
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.config.ToolManagerBuilder
import com.pdftron.pdf.controls.*
import com.pdftron.pdf.dialog.BookmarksDialogFragment
import com.pdftron.pdf.dialog.annotlist.AnnotationListSortOrder
import com.pdftron.pdf.tools.R
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.*


class MainActivity : AppCompatActivity(),
    BookmarksDialogFragment.BookmarksDialogListener,
    BookmarksTabLayout.BookmarksTabsListener {

    private var pdfView: PDFViewCtrl? = null
    private var thumbnailSlider: ThumbnailSlider? = null
    private var mBookmarksDialog: BookmarksDialogFragment? = null
    private val mBookmarkDialogCurrentTab = 0

    private lateinit var binding: ActivityMainBinding

    private var pdfUrl: String? = "https://d15je9f4aje8e5.cloudfront.net/Resources/Pdfs/Sg7hCWk1PDaDTd2KLDbqtQDcm0isGspAE62qdqZj.pdf?Expires=1651266421&Signature=IxkfI-zWbBINLU59BgKGMYtGxeAHG9mpUaqhXsBQC4yjfBgiy1JeUQy5d7cOSj6KVQ~kZS3D6a65GD-NbvypYFGVutxmLk9hj7TR6jl2UmyZ-eyvFBtqmv-HbYLWU~rS5ydVRQZyoHQRyZwL9xb0rd8dTUy90D~2fKfzI1MAhW5cBAKeKh8flG51IMdEZe8PFP0Xo3Xhil87XqOcaGeUdYJdECI5iyh1vD~7e~V4iCdP0o7cncLf1RRxZFJJHJLwKVza9yMhHBORPDH3q5mgpnuxGlRpmCzIkyjX4HoN~jEabaM5ZxZccKCwljkB0XiaJMyQOSLzvJ9JWvTO6hwxvg__&Key-Pair-Id=APKAIGE6QGVJJT73EZAQ"

    private val simplePdfUrl = "http://www.africau.edu/images/default/sample.pdf"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pdfView = binding.pdfView
        thumbnailSlider = binding.thumbnailSlider

        initUI()
    }

    private fun openFileURL() {
        try {
            //zoomSettingSample()
            AppUtils.setupPDFViewCtrl(pdfView!!)
            pdfView?.pageViewMode = PDFViewCtrl.PageViewMode.FIT_WIDTH
            pdfView?.openUrlAsync(simplePdfUrl, null, null, null)
            pdfView?.addDocumentLoadListener {
                binding.pbSamplePdf.visibility = View.GONE
            }
        } catch (e: PDFNetException) {
            e.printStackTrace()
        }
    }

    private fun initUI() {
        binding.pbSamplePdf.visibility = View.VISIBLE

        pdfView?.longPressEnabled = false
        pdfView?.isHorizontalScrollBarEnabled = true
        thumbnailSlider?.setPdfViewCtrl(pdfView)

        val barLayout: View? = thumbnailSlider?.findViewById(R.id.controls_thumbnail_slider_scrubberview)
        barLayout?.setBackgroundColor(Color.BLUE)

        thumbnailSlider!!.setOnMenuItemClickedListener { menuItemPosition: Int ->
            if (menuItemPosition == ThumbnailSlider.POSITION_LEFT) {
                //To do here
            } else {
                onNavigationListSelected()
            }
        }

        thumbnailSlider?.setThumbSliderListener(object :
            ThumbnailSlider.OnThumbnailSliderTrackingListener {
            override fun onThumbSliderStartTrackingTouch() {}
            override fun onThumbSliderStopTrackingTouch(pageNum: Int) {
                pdfView?.hasSelectionOnPage(pageNum)
                pdfView?.currentPage = pageNum
            }
        })

        val mToolManager: ToolManager = ToolManagerBuilder.from()
            .setEditInk(true)
            .setOpenToolbar(true)
            .setBuildInPageIndicator(false)
            .setCopyAnnot(true)
            .build(this, pdfView!!)
        pdfView?.toolManager = mToolManager

        pdfView?.longPressEnabled = true

        openFileURL()

    }

    private fun onNavigationListSelected() {
        mBookmarksDialog = BookmarksDialogFragment.newInstance(BookmarksDialogFragment.DialogMode.DIALOG)
        mBookmarksDialog!!.setPdfViewCtrl(pdfView!!)
            .setDialogFragmentTabs(getBookmarksDialogTabs(), mBookmarkDialogCurrentTab)
        mBookmarksDialog!!.setBookmarksDialogListener(this)
        mBookmarksDialog!!.setBookmarksTabsListener(this)
        mBookmarksDialog!!.setStyle(DialogFragment.STYLE_NO_TITLE, ThemeProvider().theme)
        mBookmarksDialog!!.show(supportFragmentManager, "bookmarks_dialog")
    }

    private fun getBookmarksDialogTabs(): ArrayList<DialogFragmentTab> {
        val userBookmarkTab = createUserBookmarkDialogTab()
        val outlineTab = createOutlineDialogTab()
        val annotationTab = createAnnotationDialogTab()
        val dialogFragmentTabs: ArrayList<DialogFragmentTab> = ArrayList(3)
        dialogFragmentTabs.add(userBookmarkTab)
        dialogFragmentTabs.add(outlineTab)
        dialogFragmentTabs.add(annotationTab)
        return dialogFragmentTabs
    }

    private fun createUserBookmarkDialogTab(): DialogFragmentTab {
        val bundle = Bundle()
        val readonly = false
        val allowEditing = true
        val allowBookmarkCreation = true
        val autoSort = true
        bundle.putBoolean(UserBookmarkDialogFragment.BUNDLE_IS_READ_ONLY, readonly)
        bundle.putBoolean(UserBookmarkDialogFragment.BUNDLE_ALLOW_EDITING, allowEditing)
        bundle.putBoolean(
            UserBookmarkDialogFragment.BUNDLE_BOOKMARK_CREATION_ENABLED,
            allowBookmarkCreation
        )
        bundle.putBoolean(UserBookmarkDialogFragment.BUNDLE_AUTO_SORT_BOOKMARKS, autoSort)
        return DialogFragmentTab(
            UserBookmarkDialogFragment::class.java,
            BookmarksTabLayout.TAG_TAB_BOOKMARK,
            Utils.getDrawable(this, R.drawable.ic_bookmarks_white_24dp),
            null,
            getString(R.string.bookmark_dialog_fragment_bookmark_tab_title),
            bundle,
            R.menu.fragment_user_bookmark
        )
    }

    /**
     * Creates the outline dialog fragment tab
     *
     * @return The outline dialog fragment tab
     */
    private fun createOutlineDialogTab(): DialogFragmentTab {
        val bundle = Bundle()
        val allowEditing = true
        bundle.putBoolean(OutlineDialogFragment.BUNDLE_EDITING_ENABLED, allowEditing)
        return DialogFragmentTab(
            OutlineDialogFragment::class.java,
            BookmarksTabLayout.TAG_TAB_OUTLINE,
            Utils.getDrawable(this, R.drawable.ic_outline_white_24dp),
            null,
            getString(R.string.bookmark_dialog_fragment_outline_tab_title),
            bundle,
            R.menu.fragment_outline
        )
    }

    /**
     * Creates the annotation dialog fragment tab
     *
     * @return The annotation dialog fragment tab
     */
    private fun createAnnotationDialogTab(): DialogFragmentTab {
        val bundle = Bundle()
        val readonly = true
        val isRTL = false
        val filterEnabled = true
        bundle.putBoolean(AnnotationDialogFragment.BUNDLE_IS_READ_ONLY, readonly)
        bundle.putBoolean(AnnotationDialogFragment.BUNDLE_IS_RTL, isRTL)
        bundle.putInt(
            AnnotationDialogFragment.BUNDLE_KEY_SORT_MODE,
            PdfViewCtrlSettingsManager.getAnnotListSortOrder(
                this,
                AnnotationListSortOrder.DATE_ASCENDING
            ) // default sort order
        )
        bundle.putBoolean(AnnotationDialogFragment.BUNDLE_ENABLE_ANNOTATION_FILTER, filterEnabled)
        return DialogFragmentTab(
            AnnotationDialogFragment::class.java,
            BookmarksTabLayout.TAG_TAB_ANNOTATION,
            Utils.getDrawable(this, R.drawable.ic_annotations_white_24dp),
            null,
            getString(R.string.bookmark_dialog_fragment_annotation_tab_title),
            bundle,
            R.menu.fragment_annotlist_sort
        )
    }
    override fun onBookmarksDialogWillDismiss(tabIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun onBookmarksDialogDismissed(tabIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun onUserBookmarkClick(pageNum: Int) {
        TODO("Not yet implemented")
    }

    override fun onOutlineClicked(parent: Bookmark?, bookmark: Bookmark?) {
        TODO("Not yet implemented")
    }

    override fun onAnnotationClicked(annotation: Annot?, pageNum: Int) {
        TODO("Not yet implemented")
    }

    override fun onExportAnnotations(outputDoc: PDFDoc?) {
        TODO("Not yet implemented")
    }

    override fun onEditBookmarkFocusChanged(isActive: Boolean) {
        TODO("Not yet implemented")
    }
}