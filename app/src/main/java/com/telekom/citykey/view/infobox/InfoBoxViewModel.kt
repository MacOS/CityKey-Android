/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.view.infobox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.mailbox.MailboxManager
import com.telekom.citykey.data.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.content.City
import com.telekom.citykey.networkinterface.models.user.InfoBoxContent
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber

class InfoBoxViewModel(
    private val globalData: GlobalData,
    private val mailboxManager: MailboxManager
) : BaseViewModel() {
    val content: LiveData<Pair<List<InfoboxItem>, List<InfoboxItem>>> get() = _content
    val loggedInState: LiveData<Boolean> get() = _loggedInState
    val shouldPromptLogin: LiveData<Boolean> get() = _shouldPromptLogin
    val state: LiveData<InfoBoxStates> get() = _state
    val deletionSuccessful: LiveData<Boolean> get() = _deletionSuccessful
    val showNoInternetDialog: LiveData<Unit> get() = _showNoInternetDialog
    val infoboxCount: LiveData<Int?> get() = _infoboxCount
    var messageId: Int? = null

    val cityData: LiveData<City> get() = globalData.city.toFlowable(BackpressureStrategy.LATEST).toLiveData()

    private val _content: MutableLiveData<Pair<List<InfoboxItem>, List<InfoboxItem>>> = MutableLiveData()
    private val _state: MutableLiveData<InfoBoxStates> = MutableLiveData()
    private val _loggedInState: MutableLiveData<Boolean> = MutableLiveData()
    private val _shouldPromptLogin: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _deletionSuccessful: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _showNoInternetDialog: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _infoboxCount: SingleLiveEvent<Int> = SingleLiveEvent()

    private var deletionDisposable: Disposable? = null

    init {
        launch {
            mailboxManager.userInfoBox
                .map {
                    val allMails = it.map { info -> info.copy() }.map(InfoboxItem::Mail)
                    val unreadMails = allMails.filter { info -> !info.item.isRead }

                    val allItems = allMails.ifEmpty { listOf<InfoboxItem>(InfoboxItem.Empty) }
                    val unreadItems = unreadMails.ifEmpty { listOf<InfoboxItem>(InfoboxItem.Empty) }
                    allItems to unreadItems
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { _state.postValue(InfoBoxStates.OK) }
                .subscribe {
                    _content.postValue(it)
                    try {
                        if (globalData.isUserLoggedIn)
                            _infoboxCount.postValue(
                                (it.first.filter { info -> (info as InfoboxItem.Mail).item.isRead }?.size ?: 0)
                            )
                        else
                            _infoboxCount.postValue(0)
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }

        }
        launch { globalData.user.map { it is UserState.Present }.subscribe(_loggedInState::postValue) }

        launch {
            mailboxManager.errorState
                .map { if (_content.value != null) InfoBoxStates.REFRESH_ERROR else it }
                .subscribe(_state::postValue)
        }
    }

    fun onRefresh() {
        mailboxManager.refreshInfoBox()
    }

    fun onUndoDeletion() {
        deletionDisposable?.dispose()
        launch {
            mailboxManager.restoreDeletedMsg()
                .subscribe({ Timber.i("Last message restored !") }, this::onSwipeGestureNetworkError)
        }
    }

    fun onToggleRead(read: Boolean, id: Int) {
        launch {
            mailboxManager.toggleInfoRead(id, read)
                .subscribe({}, this::onSwipeGestureNetworkError)
        }
    }

    fun onDelete(msg: InfoBoxContent) {
        deletionDisposable = mailboxManager.deleteMessage(msg)
            .subscribe(
                {
                    Timber.i("Message with ID = ${msg.userInfoId} deleted !")
                    _deletionSuccessful.postValue(true)
                },
                this::onSwipeGestureNetworkError
            )
            .also { launch { it } }
    }

    private fun onSwipeGestureNetworkError(throwable: Throwable) {
        when (throwable) {
            is InvalidRefreshTokenException -> {
                globalData.logOutUser(throwable.reason)
                _shouldPromptLogin.postValue(true)
            }

            is NoConnectionException -> _showNoInternetDialog.call()
        }
    }
}
