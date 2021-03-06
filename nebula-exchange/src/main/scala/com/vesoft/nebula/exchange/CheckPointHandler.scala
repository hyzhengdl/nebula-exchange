/* Copyright (c) 2020 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.exchange

import com.vesoft.nebula.exchange.config.{SourceCategory}
import com.vesoft.nebula.exchange.utils.HDFSUtils
import com.vesoft.nebula.exchange.config.SchemaConfigEntry
import org.apache.spark.TaskContext

/**
  * CheckPointHandler handle the checkpoint files for Neo4j and Janusgraph
  */
object CheckPointHandler {

  def checkSupportResume(value: SourceCategory.Value): Boolean = {
    value match {
      case SourceCategory.NEO4J       => true
      case SourceCategory.JANUS_GRAPH => true
      case _                          => false
    }
  }

  def getPathAndOffset(schemaConfig: SchemaConfigEntry,
                       breakPointCount: Long): Option[(String, Long)] = {
    val partitionId = TaskContext.getPartitionId()
    if (checkSupportResume(schemaConfig.dataSourceConfigEntry.category) && schemaConfig.checkPointPath.isDefined) {
      val path   = s"${schemaConfig.checkPointPath.get}/${schemaConfig.name}.${partitionId}"
      val offset = breakPointCount + fetchOffset(path)
      Some((path, offset))
    } else {
      None
    }
  }

  def fetchOffset(path: String): Long = {
    HDFSUtils.getContent(path).toLong
  }
}
