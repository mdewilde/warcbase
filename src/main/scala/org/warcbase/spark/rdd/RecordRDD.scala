package org.warcbase.spark.rdd

import org.apache.spark.rdd.RDD
import org.warcbase.spark.matchbox.ExtractTopLevelDomain
import org.warcbase.spark.matchbox.RecordTransformers.WARecord

import scala.reflect.ClassTag

/**
  * A Wrapper class around RDD to allow RDDs of type ARCRecord and WARCRecord to be queried via a fluent API.
  *
  * To load such an RDD, please see [[org.warcbase.spark.matchbox.RecordLoader]]
  */
object RecordRDD extends java.io.Serializable {

  implicit class CountableRDD[T: ClassTag](rdd: RDD[T]) extends java.io.Serializable {
    def countItems(): RDD[(T, Int)] = {
      rdd.map(r => (r, 1))
        .reduceByKey((c1, c2) => c1 + c2)
        .sortBy(f => f._2, ascending = false)
    }
  }

  implicit class WARecordRDD(rdd: RDD[WARecord]) extends java.io.Serializable {

    def keepValidPages(): RDD[WARecord] = {
      rdd.discardDate(null).keepMimeTypes(Set("text/html"))
    }

    def keepMimeTypes(mimeTypes: Set[String]) = {
      rdd.filter(r => mimeTypes.contains(r.getMimeType))
    }

    def keepDate(date: String) = {
      rdd.filter(r => r.getCrawldate == date)
    }

    def keepUrls(urls: Set[String]) = {
      rdd.filter(r => urls.contains(r.getUrl))
    }

    def keepDomains(urls: Set[String]) = {
      rdd.filter(r => urls.contains(ExtractTopLevelDomain(r.getUrl).replace("^\\s*www\\.", "")))
    }

    def discardMimeTypes(mimeTypes: Set[String]) = {
      rdd.filter(r => !mimeTypes.contains(r.getMimeType))
    }

    def discardDate(date: String) = {
      rdd.filter(r => r.getCrawldate != date)
    }

    def discardUrls(urls: Set[String]) = {
      rdd.filter(r => !urls.contains(r.getUrl))
    }

    def discardDomains(urls: Set[String]) = {
      rdd.filter(r => !urls.contains(r.getDomain))
    }
  }

}
