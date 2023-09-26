/* Copyright (c) 2023 DeflatedPickle under the GPLv3 license */

@file:Suppress("SpellCheckingInspection", "FunctionName")

package com.deflatedpickle.somftcraft.api

interface DoesAge {
    fun `somftcraft$isBaby`(): Boolean
    fun `somftcraft$isAdult`(): Boolean
    fun `somftcraft$onGrowUp`()
}
