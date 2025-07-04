package com.bridge.androidtechnicaltest.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.db.SyncStatus
import com.bridge.androidtechnicaltest.viewmodel.PupilListViewModel
import com.bridge.androidtechnicaltest.viewmodel.PupilSharedViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val viewModel: PupilListViewModel by viewModel()
    private val sharedViewModel: PupilSharedViewModel by viewModel()
    private lateinit var syncStatusContainer: LinearLayout
    private lateinit var syncStatusText: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val fm = supportFragmentManager
            fm.beginTransaction()
                    .add(R.id.container, PupilListFragment())
                    .commit()
        }
        syncStatusContainer = findViewById(R.id.syncStatusContainer)
        syncStatusText = findViewById(R.id.syncStatusText)

        lifecycleScope.launch {
            sharedViewModel.unSyncedCount.collect { count ->
                if (count > 0) {
                    syncStatusContainer.visibility = View.VISIBLE
                    syncStatusText.text = "$count unsynced pupil(s). Tap to sync."
                    syncStatusContainer.setOnClickListener {
                        viewModel.syncPendingPupils()
                    }
                } else {
                    syncStatusContainer.visibility = View.GONE
                }
            }
        }

        viewModel.observeSyncStatus().observe(this) { syncStatus ->
            when (syncStatus) {
                SyncStatus.PENDING -> {
                    syncStatusContainer.visibility = View.VISIBLE
                    syncStatusText.text = "Sync pending. Tap to sync."
                    syncStatusContainer.setOnClickListener {
                        viewModel.syncPendingPupils()
                    }
                }

                SyncStatus.SUCCESS -> {
                    syncStatusContainer.visibility = View.GONE
                }

                SyncStatus.ERROR -> {
                    syncStatusContainer.visibility = View.VISIBLE
                    syncStatusText.text = getString(R.string.sync_error_tap_to_retry)
                    syncStatusContainer.setOnClickListener {
                        viewModel.syncPendingPupils()
                    }
                }

                SyncStatus.SYNCING -> {
                    syncStatusContainer.visibility = View.VISIBLE
                    syncStatusText.text = getString(R.string.syncing)
                    syncStatusContainer.setOnClickListener {}
                }
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_reset) {
            lifecycleScope.launch {
                viewModel.clearAllData()
                viewModel.loadPupils()
            }
            return true
        } else if (item.itemId == R.id.action_sync) {
            viewModel.syncPendingPupils()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}