package test

import scala.io.Source

/**
  * Created by C.J.YOU on 2016/8/19.
  */
object Test {


  def main(args: Array[String]) {


    val temp = "Mozilla/4.0"

    val stopWords = List("AppleWebKit","Dalvik")

    Source.fromFile("F:\\workspace\\mall_xinfushe_com_access.log").getLines().map(getUserAgent).filter(_ != "None").map(_.split(";"))
      .filter(_.length >= 1).map(getOsAndOsType).filter(x => x._1 != x._2).foreach(println)


  }


  def getUserAgent(string: String): String = {

    var res = ""

    if(string.contains("Mozilla")) {

      val regex = "[\\(（][\\s\\S]*[\\)）]".r

      val result = regex.findFirstIn( string.substring(string.indexOf("Mozilla")).split("\\)")(0) + ")").getOrElse("None")

      res = result

    }

    res
  }

  def getOsAndOsType(array: Array[String]): (String, String) ={

    val res = array.filter(_.length > "iphone".length).map(_.replace("(","").replace(")",""))

    if(res.length > 1)
      (res(0),res(1))
    else if(res.length == 1)
      (res(0),"None")
    else
      ("None", "None")
  }

}
