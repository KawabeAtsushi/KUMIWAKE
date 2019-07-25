/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pandatone.kumiwake.kumiwake

import android.os.Parcel
import android.os.Parcelable

class Category : Parcelable {

    val name: String?
    val id: String?
    val theme: Theme

    constructor(name: String, id: String, theme: Theme) {
        this.name = name
        this.id = id
        this.theme = theme
    }

    private constructor(`in`: Parcel) {
        name = `in`.readString()
        id = `in`.readString()
        theme = Theme.values()[`in`.readInt()]
    }

    override fun toString(): String {
        return "Category{" +
                "mName='" + name + '\''.toString() +
                ", mId='" + id + '\''.toString() +
                ", mTheme=" + theme +
                '}'.toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(id)
        dest.writeInt(theme.ordinal)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val category = other as Category?

        if (id != category!!.id) {
            return false
        }
        if (name != category.name) {
            return false
        }
        return theme == category.theme

    }

    override fun hashCode(): Int {
        var result = name!!.hashCode()
        result = 31 * result + id!!.hashCode()
        result = 31 * result + theme.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }

}
