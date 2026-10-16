package com.opus.piano

import com.opus.piano.data.PracticeCsv
import org.junit.Assert.*
import org.junit.Test
import java.io.StringWriter

class CsvExportTest {
    @Test fun unicodeQuotesCommasAndMultilineArePreserved() {
        val out=StringWriter();PracticeCsv.row(out,listOf("Dvořák, étude","say \"hello\"\nnext",123,null))
        assertEquals("\"Dvořák, étude\",\"say \"\"hello\"\"\nnext\",123,\r\n",out.toString())
    }
    @Test fun spreadsheetFormulasAreNeutralizedWithoutChangingNumbers() {
        assertEquals("\"'=1+1\"",PracticeCsv.field("=1+1"))
        assertEquals("\"'  @SUM(A1)\"",PracticeCsv.field("  @SUM(A1)"))
        assertEquals("-2",PracticeCsv.field(-2))
        assertFalse("elapsed_ms" in PracticeCsv.partColumns)
        assertEquals("",PracticeCsv.field(null))
    }
}
