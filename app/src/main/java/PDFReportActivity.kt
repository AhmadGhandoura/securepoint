package com.example.securepoint

import android.os.Bundle
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class PDFReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdfreport_activity) //Make sure this layout file exists

        //Call the function to generate the PDF
        generatePDF(this)
    }
    private fun generatePDF( context:Context) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder( 300,600,1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()
        paint.textSize = 16f
        canvas.drawText("This is your PDF content", 50f, 50f, paint)

        document.finishPage(page)

        val file = File(getExternalFilesDir(null), "myFIle.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
    }
}