package com.opus.piano

import com.opus.piano.data.appendPieceAnnotation
import org.junit.Assert.*
import org.junit.Test

class Polish101Test {
    @Test fun untimedPieceAnnotationPreservesOriginalAndLabelsItsScope() {
        assertEquals("Original\n\nPrelude:\nWork on phrasing",appendPieceAnnotation("Original","Prelude","Work on phrasing"))
        assertEquals("Original",appendPieceAnnotation("Original",null,"Ignored"))
        assertEquals("Original",appendPieceAnnotation("Original","Prelude",""))
    }
    @Test fun oversizedAnnotationIsRejectedInsteadOfTruncatingSavedNotes() {
        val original="x".repeat(9999)
        try {appendPieceAnnotation(original,"Piece","New");fail("Must preserve all text or reject")}
        catch(_:IllegalArgumentException) {}
        assertEquals(9999,original.length)
    }
}
