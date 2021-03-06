/*
 * Copyright (c) 2020. Carlos René Ramos López. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crrl.beatplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.crrl.beatplayer.R
import com.crrl.beatplayer.alertdialog.AlertDialog
import com.crrl.beatplayer.alertdialog.dialogs.AlertItemAction
import com.crrl.beatplayer.alertdialog.stylers.AlertItemStyle
import com.crrl.beatplayer.alertdialog.stylers.AlertItemTheme
import com.crrl.beatplayer.alertdialog.stylers.AlertType
import com.crrl.beatplayer.extensions.addFragment
import com.crrl.beatplayer.extensions.getColorByTheme
import com.crrl.beatplayer.extensions.observe
import com.crrl.beatplayer.extensions.safeActivity
import com.crrl.beatplayer.models.Album
import com.crrl.beatplayer.ui.adapters.AlbumAdapter
import com.crrl.beatplayer.ui.fragments.base.BaseFragment
import com.crrl.beatplayer.ui.viewmodels.AlbumViewModel
import com.crrl.beatplayer.utils.GeneralUtils
import com.crrl.beatplayer.utils.GeneralUtils.VERTICAL
import com.crrl.beatplayer.utils.PlayerConstants
import com.crrl.beatplayer.utils.SettingsUtility
import com.crrl.beatplayer.utils.SortModes
import kotlinx.android.synthetic.main.fragment_album.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AlbumFragment : BaseFragment<Album>() {

    private val viewModel: AlbumViewModel by viewModel { parametersOf(context) }
    private lateinit var albumAdapter: AlbumAdapter

    companion object {
        fun newInstance() = AlbumFragment()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    private fun init(view: View) {
        val sc = if (GeneralUtils.getRotation(safeActivity) == VERTICAL) 2 else 5

        // Init Adapter
        albumAdapter = AlbumAdapter(context).apply {
            showHeader = true
            itemClickListener = this@AlbumFragment
            spanCount = sc
        }

        // Set up RecyclerView
        view.album_list.apply {
            layoutManager = GridLayoutManager(context, sc).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) sc else 1
                    }
                }
            }
            adapter = albumAdapter
        }

        dialog = buildSortModesDialog()

        viewModel.getAlbums()!!.observe(this) { list ->
            albumAdapter.updateDataSet(list)
        }
    }

    private fun buildSortModesDialog(): AlertDialog {
        val style = AlertItemStyle()
        style.apply {
            textColor = activity?.getColorByTheme(R.attr.titleTextColor, "titleTextColor")!!
            selectedTextColor = activity?.getColorByTheme(R.attr.colorAccent, "colorAccent")!!
            backgroundColor =
                activity?.getColorByTheme(R.attr.colorPrimarySecondary2, "colorPrimarySecondary2")!!
        }
        return AlertDialog(
            getString(R.string.sort_title),
            getString(R.string.sort_msg),
            style,
            AlertType.BOTTOM_SHEET
        ).apply {
            addItem(AlertItemAction(
                context!!.getString(R.string.sort_default),
                SettingsUtility.getInstance(context).albumSortOrder == SortModes.AlbumModes.ALBUM_DEFAULT,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).albumSortOrder =
                    SortModes.AlbumModes.ALBUM_DEFAULT
                reloadAdapter()
            })
            addItem(AlertItemAction(
                context!!.getString(R.string.sort_az),
                SettingsUtility.getInstance(context).albumSortOrder == SortModes.AlbumModes.ALBUM_A_Z,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).albumSortOrder = SortModes.AlbumModes.ALBUM_A_Z
                reloadAdapter()
            })
            addItem(AlertItemAction(
                context!!.getString(R.string.sort_za),
                SettingsUtility.getInstance(context).albumSortOrder == SortModes.AlbumModes.ALBUM_Z_A,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).albumSortOrder = SortModes.AlbumModes.ALBUM_Z_A
                reloadAdapter()
            })
            addItem(AlertItemAction(
                context!!.getString(R.string.sort_year),
                SettingsUtility.getInstance(context).albumSortOrder == SortModes.AlbumModes.ALBUM_YEAR,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).albumSortOrder =
                    SortModes.AlbumModes.ALBUM_YEAR
                reloadAdapter()
            })
        }
    }

    private fun reloadAdapter() {
        viewModel.update()
    }

    override fun onItemClick(view: View, position: Int, item: Album) {
        val extras = Bundle()
        extras.putString(PlayerConstants.ALBUM_KEY, item.toString())
        activity!!.addFragment(
            R.id.nav_host_fragment,
            AlbumDetailFragment(),
            PlayerConstants.ALBUM_DETAIL,
            true,
            extras
        )
    }

    override fun onPopupMenuClick(view: View, position: Int, item: Album) {
        Toast.makeText(context, "Menu of " + item.title, Toast.LENGTH_SHORT).show()
    }

    override fun onPlayAllClick(view: View) {
        Toast.makeText(context, "Shuffle", Toast.LENGTH_SHORT).show()
    }

    override fun onSortClick(view: View) {
        dialog.show(activity as AppCompatActivity)
    }
}
