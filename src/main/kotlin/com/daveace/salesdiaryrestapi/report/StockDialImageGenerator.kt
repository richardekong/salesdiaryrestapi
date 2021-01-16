package com.daveace.salesdiaryrestapi.report

import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.mapper.Mappable
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.DialChart
import org.knowm.xchart.DialChartBuilder
import org.knowm.xchart.style.Styler
import reactor.core.publisher.Flux
import java.awt.Color
import java.io.File

class StockDialImageGenerator {

    companion object {
        private const val IMAGE_PATH = "src/main/resources/static/product-gauge/"

        fun generateDialImage(products: Flux<Product>): Flux<DialImagePath> {
            return products.map {
                createDialImageFile("$IMAGE_PATH${it.id}", it)
            }
        }

        private fun createDialImageFile(filePath: String, product: Product): DialImagePath {
            val chart: DialChart = createProductDialChart(product)
            val imageFile = File("${filePath}.png")
            BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG)
            if (!(imageFile.isFile && imageFile.exists()))
                throw RuntimeException("Failed to create dial image file")
            return DialImagePath(imageFile.path)
        }

        private fun createProductDialChart(product: Product): DialChart {
            val dialLevel: Double = product.stock / product.maxStock
            val chart: DialChart = DialChartBuilder()
                .width(300)
                .height(300)
                .title(product.name)
                .theme(Styler.ChartTheme.XChart)
                .build()
            chart.addSeries(
                "Stock (%)",
                dialLevel,
                "${dialLevel * product.maxStock} out of ${product.maxStock}"
            )
            styleChart(chart)
            return chart
        }

        private fun styleChart(chart: DialChart) {
            chart.styler.apply {
                isLegendVisible = false
                normalColor = Color.YELLOW
                redFrom = 0.0
                redTo = 0.3
                normalFrom = 0.3
                normalTo = 0.7
                greenFrom = 0.7
                greenTo = 1.0
                chartBackgroundColor = Color(102, 51, 153, 170)
                plotBackgroundColor = Color.BLACK
                chartFontColor = Color.WHITE
                axisTickMarksColor = Color.black
                chartPadding = 5
                antiAlias = true
            }
        }

        data class DialImagePath(val path: String) : Mappable
    }
}

