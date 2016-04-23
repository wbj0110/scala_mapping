package main.scala.com.xiaomishu.mapping

import _root_.scopt.OptionParser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Get, HTable}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.log4j.PropertyConfigurator

import scala.io.Source

/**
 * Created by wengbenjue on 2014/9/16.
 */
object Mapping2Hbase {
  private  var hb = HbaseTool
  var confHbase: Configuration = null

  {
    //PropertyConfigurator.configure("log4j.properties");//加载.properties文件

    //lond the config of Hbase，create Table recomend
    confHbase = HBaseConfiguration.create()
    confHbase.set("hbase.zookeeper.property.clientPort", "2181")
    confHbase.set("hbase.zookeeper.quorum", "spark1.soledede.com,spark2.soledede.com,spark3.soledede.com")
    confHbase.set("hbase.master", "spark1.soledede.com:60000")
    confHbase.addResource("/opt/cloudera/parcels/CDH/lib/hbase/conf/hbase-site.xml")
    confHbase.set(TableInputFormat.INPUT_TABLE, "recomend")

  }

  case class Params(
                     input: String = "C:\\Users\\Administrator\\Downloads\\resmapping.txt",
                     hbase_mapping_table:String = "mapping",
                     hbase_columnfamily:String = "fruit",
                     file_separator:String = "\\t",
                     zookeeper_quorum: String = "spark1.soledede.com,spark2.soledede.com,spark3.soledede.com")

  def main(args: Array[String]) {
    val defaultParams = Params()

    val parser = new OptionParser[Params]("Mapping2Hbase") {
      head("Mapping resid from int to string")
      opt[String]("hbase_mapping_table")
        .text(s"hbase_mapping_table, default: ${defaultParams.hbase_mapping_table}")
        .action((x, c) => c.copy(hbase_mapping_table = x))
      opt[String]("hbase_columnfamily")
        .text(s"hbase_columnfamily, default: ${defaultParams.hbase_columnfamily}")
        .action((x, c) => c.copy(hbase_columnfamily = x))
      opt[String]("file_separator")
        .text(s"file_separator, default: ${defaultParams.file_separator}")
        .action((x, c) => c.copy(zookeeper_quorum = x))
      opt[String]("zookeeper_quorum")
        .text(s"zookeeper_quorum, default: ${defaultParams.zookeeper_quorum}")
        .action((x, c) => c.copy(zookeeper_quorum = x))
      arg[String]("<input>")
        .required()
        .text("input paths to a res_mapping file")
        .action((x, c) => c.copy(input = x))
      note(
        """
          |For example, the following command runs this app on a synthetic dataset:
          |
          |  java -jar xxx.jar --zookeeper_quorum  spark1.soledede.com,spark2.soledede.com,spark3.soledede.com" \
          |  /home/hadoop/recomend/res_mapping.txt
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params =>
      run(params)
    } getOrElse {
      System.exit(1)
    }
  }


  def run(params: Params) {
    confHbase.set("hbase.zookeeper.quorum", params.zookeeper_quorum);
    Mapping2Hbase.hb.setConf(confHbase)
    Mapping2Hbase.hb.createTable(params.hbase_mapping_table, params.hbase_columnfamily);
    /**
    Source.fromFile(params.input).getLines().map { line =>
      val fields = line.split(params.file_separator)
      Mapping2Hbase.hb.putSingleValue(params.hbase_mapping_table,fields(0),params.hbase_columnfamily,"fruitId",fields(1))
      (fields(0),fields(1))
     // println(fields(0)+":"+fields(1))
    }
   //listMp.foreach(println)
    println("res_mapping入库完成")
    // listMp.map( (Mapping2Hbase.hb.putSingleValue("mapping",_._1,"res","resid",_._2)))

      */

    for(line <- Source.fromFile(params.input).getLines()){
        val fields = line.split(params.file_separator)
      Mapping2Hbase.hb.putSingleValue(params.hbase_mapping_table,fields(0),params.hbase_columnfamily,"resid",fields(1)+"::"+fields(2))
      println(fields(0)+"::"+fields(1)+"::"+fields(2))
    }


    //Mapping2Hbase.hb.getValue("mapping",rowKey:String,family:String,qualifiers:Array[String])
  }
}


