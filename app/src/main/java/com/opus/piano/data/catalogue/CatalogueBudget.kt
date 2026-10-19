package com.opus.piano.data.catalogue

/** Space reservations limit disposable acquisition only; never prevent personal export/recovery. */
internal object CatalogueBudget {
    const val MIN_RESERVE=128L*1024*1024
    const val CRITICAL_EXPORT=32L*1024*1024
    const val JOURNAL_GROWTH=32L*1024*1024
    private fun add(a:Long,b:Long)=if(Long.MAX_VALUE-a<b)Long.MAX_VALUE else a+b
    fun reserve(personalBytes:Long,otherCriticalBytes:Long=0):Long {
        require(personalBytes>=0 && otherCriticalBytes>=0)
        return maxOf(MIN_RESERVE,add(add(personalBytes,otherCriticalBytes),2*CRITICAL_EXPORT+JOURNAL_GROWTH))
    }
    fun fits(freeBytes:Long,estimatedBytes:Long,reserveBytes:Long):Boolean =
        freeBytes>=0 && estimatedBytes>=0 && reserveBytes>=0 && estimatedBytes<=freeBytes && freeBytes-estimatedBytes>=reserveBytes
}
