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

package com.telekom.citykey.pictures

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

fun ImageView.loadFromOSCA(imageUrl: String?) {
    if (imageUrl.isNullOrBlank()) return
    GlideApp.with(context)
        .load(
            "${BuildConfig.IMAGE_URL}$imageUrl".toUri()
        )
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.loadFromOSCA(imageUrl: String, placeholderResId: Int = 0, errorResId: Int = 0) {
    var glideRequestRemoteImage = GlideApp.with(context)
        .load(
            "${BuildConfig.IMAGE_URL}$imageUrl".toUri()
        )
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
    if (placeholderResId != 0) {
        glideRequestRemoteImage = glideRequestRemoteImage.placeholder(placeholderResId)
    }
    if (errorResId != 0) {
        glideRequestRemoteImage = glideRequestRemoteImage.error(errorResId)
    } else if (placeholderResId != 0) {
        glideRequestRemoteImage = glideRequestRemoteImage.error(placeholderResId)
    }
    glideRequestRemoteImage.into(this)
}

fun ImageView.loadFromURL(
    imageUrl: String?,
    cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.ALL
) {
    imageUrl?.let {
        GlideApp.with(context)
            .load(it.toUri())
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .centerInside()
            .diskCacheStrategy(cacheStrategy)
            .into(this)
    }
}

fun ImageView.loadFromURLwithProgress(
    imageUrl: String?,
    successListener: (Boolean) -> Unit,
    cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.ALL
) {
    imageUrl?.let {
        GlideApp.with(context)
            .load(it.toUri())
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .centerInside()
            .placeholder(R.drawable.ic_bg_detail_page)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    successListener.invoke(true)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    successListener.invoke(false)
                    return false
                }
            })
            .diskCacheStrategy(cacheStrategy)
            .into(this)
    }
}

fun ImageView.loadFromDrawable(@DrawableRes resourceId: Int) {
    GlideApp.with(context)
        .load(resourceId)
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(this)
}

fun ImageView.loadCenterCropped(imageReference: String?) {
    imageReference ?: return
    GlideApp.with(context)
        .load(BuildConfig.IMAGE_URL + imageReference)
        .centerCrop()
        .into(this)
}

fun ImageView.loadCenterCroppedWithTransform(imageReference: String?) {
    imageReference ?: return
    GlideApp.with(context)
        .load(BuildConfig.IMAGE_URL + imageReference)
        .centerCrop()
        .apply(RequestOptions.circleCropTransform())
        .into(this)
}
