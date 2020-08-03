package com.liao.camera.helper

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.app_bar.*


fun AppCompatActivity.toolbar(title: String){
    var toolbar = this.toolbar
    toolbar.title = title
    this.setSupportActionBar(toolbar)
    this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
}

