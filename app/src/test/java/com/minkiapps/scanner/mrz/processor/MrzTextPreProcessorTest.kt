package com.minkiapps.scanner.mrz.processor

import com.innovatrics.mrz.MrzParser
import com.minkiapps.scanner.id.processor.MrzTextPreProcessor
import org.junit.Assert
import org.junit.Test

class MrzTextPreProcessorTest {

    @Test
    fun processTest1() {
        val text = """IDD<TO159C 2 MN 1<<<< <<<<<<<<<
            6408125<2704058D<<<KK<<<<<K<<2
            MUSTERMANN <ERIKA<<<<KKK<<<<K<<
        """.trimIndent()

        val processed = MrzTextPreProcessor.process(text)
        Assert.assertNotNull(MrzParser.parse(processed))
    }

    @Test
    fun processTest2() {
        val text = """PKTWNLIN<<MEIKHUA<K<K<<<<K<KK<K<<<<<<<<K<<<<
    0000000000TWN7601015F1404018A234567893<<<<18
        """.trimIndent()

        val processed = MrzTextPreProcessor.process(text)
        Assert.assertNotNull(MrzParser.parse(processed))
    }

    @Test
    fun processTest3() {
        val text = """ARAUTA392103578<<<<<<<<<<<<<<<
    9309304 M2407 027CHN 162318<<<<<5
    PAN<< MINGKANG<<<<<<<< <<<<<<<<<
        """.trimIndent()

        val processed = MrzTextPreProcessor.process(text)
        Assert.assertNotNull(MrzParser.parse(processed))
    }

    @Test
    fun processTest4() {
        val text = """IDD<<T0159C2MN1<<<<<<<<<<<<<<-
    6408125<2704058D<<<<<<<<<<<<<<
    MUSTERMANN<ERIKA<<<<<<<<<<<<<<
        """.trimIndent()

        val processed = MrzTextPreProcessor.process(text)
        Assert.assertNotNull(MrzParser.parse(processed))
    }

    @Test
    fun processTest5() {
        val text = """P<AUTSCHBNABL<<CHRISTOPH<<<<<<<<<<<<<<<<<<<<
            U1358126 <8AUT0012197M2610036<<<<<<<<<<<<<<<4
        """.trimIndent()

        val processed = MrzTextPreProcessor.process(text)
        Assert.assertNotNull(MrzParser.parse(processed))
    }
}