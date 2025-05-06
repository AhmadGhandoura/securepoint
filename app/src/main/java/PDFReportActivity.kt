package com.example.securepoint

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.database.FirebaseDatabase
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.util.*

class PDFReportActivity : AppCompatActivity() {

    private lateinit var generateBtn: Button
    private lateinit var viewBtn: Button
    private lateinit var pdfFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdfreport_activity)

        generateBtn = findViewById(R.id.btn_generate_pdf)
        viewBtn = findViewById(R.id.btn_view_pdf)

        pdfFile = File(getExternalFilesDir(null), "securepoint_report.pdf")

        generateBtn.setOnClickListener {
            generatePDF()
        }

        viewBtn.setOnClickListener {
            viewPdf()
        }
    }

    private fun generatePDF() {
        try {
            val writer = PdfWriter(pdfFile)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            // Title
            val title = Paragraph("🔐 SECUREPOINT DAILY REPORT")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
                .setBold()
            document.add(title)

            document.add(LineSeparator(SolidLine()).setMarginTop(10f).setMarginBottom(10f))

            val sensorsRef = FirebaseDatabase.getInstance().getReference("sensors")
            val doorRef = FirebaseDatabase.getInstance().getReference("door")

            sensorsRef.get().addOnSuccessListener { sensorSnap ->
                val motion = sensorSnap.child("motion").getValue(Int::class.java) ?: 0
                val vibration = sensorSnap.child("vibration").getValue(Boolean::class.java) ?: false
                val gas = sensorSnap.child("gasAnalog").getValue(Long::class.java) ?: 0L
                val gasAlert = sensorSnap.child("gasAlert").getValue(Boolean::class.java) ?: false
                val window = sensorSnap.child("window").getValue(Int::class.java) ?: 0

                document.add(
                    Paragraph("🚶 Motion: ")
                        .add(Text(if (motion == 1) "Detected" else "No Motion"))
                        .setFontSize(14f)
                )
                document.add(
                    Paragraph("💥 Vibration: ")
                        .add(Text(if (vibration) "Yes" else "No"))
                        .setFontSize(14f)
                )
                document.add(
                    Paragraph("🔥 Gas Level: ")
                        .add(Text("$gas ppm"))
                        .setFontSize(14f)
                )
                document.add(
                    Paragraph("🚨 Gas Alert: ")
                        .add(Text(if (gasAlert) "YES" else "No"))
                        .setFontSize(14f)
                )
                document.add(
                    Paragraph("🪟 Window: ")
                        .add(Text(if (window == 1) "OPEN" else "Closed"))
                        .setFontSize(14f)
                )

                document.add(LineSeparator(SolidLine()).setMarginTop(10f).setMarginBottom(10f))

                // Door data
                doorRef.get().addOnSuccessListener { doorSnap ->
                    val locked = doorSnap.child("locked").getValue(Boolean::class.java) ?: true
                    val lastAccess = doorSnap.child("lastAccess").getValue(String::class.java) ?: "Unknown"

                    document.add(
                        Paragraph("🚪 Door Status: ")
                            .add(Text(if (locked) "🔒 Locked" else "🔓 Unlocked"))
                            .setFontSize(14f)
                    )
                    document.add(
                        Paragraph("📌 Last Access: ")
                            .add(Text(lastAccess))
                            .setFontSize(14f)
                    )

                    document.add(LineSeparator(SolidLine()).setMarginTop(10f))

                    document.add(
                        Paragraph("📅 Report generated: ${Date()}")
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setFontSize(10f)
                            .setFontColor(ColorConstants.GRAY)
                    )

                    document.close()
                    Toast.makeText(this, "✅ PDF saved at ${pdfFile.path}", Toast.LENGTH_LONG).show()
                    viewBtn.visibility = Button.VISIBLE
                }.addOnFailureListener {
                    document.add(Paragraph("❌ Failed to load door data"))
                    document.close()
                    Toast.makeText(this, "❌ Error reading door data", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                document.add(Paragraph("❌ Failed to load sensor data"))
                document.close()
                Toast.makeText(this, "❌ Error reading sensor data", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "❌ PDF Generation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun viewPdf() {
        if (!pdfFile.exists()) {
            Toast.makeText(this, "PDF not found. Please generate it first.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", pdfFile)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
}