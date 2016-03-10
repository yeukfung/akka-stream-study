package net.amoeba.study

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object AkkaFlow {

  // a simple class to store x and y, with result empty intially
  case class Sum(x:Int, y:Int, result:Option[Int] = None) {
    def clearResult = this.copy(result = None)
    def renderResult = this.copy(result = Some( x + y ))
    def isRendered = result.isDefined
    override def toString = if(isRendered) s"sum of $x + $y = ${result.get}" else "result not rendered yet"
  }

  /** Sources **/
  val numberList = Source(1 to 15)
  val number2List = Source(21 to 30)

  val number = Source.single(100)

  val strList = Source(List("A", "B", "C"))

  /** Flows **/
  val tupleSummer:Flow[(Int, Int), Sum, Boolean] = Flow[(Int, Int)].map( x => Sum(x._1 , x._2)).mapMaterializedValue( (Unit) => true )
  val multiplyValues = Flow[(Int, Int)].map( x => x._1 * x._2)

  /** Sinks **/
  def out[T] = Sink.foreach[T](println(_))
  def quotedOut[T] = Sink.foreach[T]( x => {
    println("=== start ===") 
    println(x)
    println("--- end ---")
  })

  // for each sum returned, render the result, and then return the result as material 
  val renderSum = Sink.fold[Int, Sum](0)( (acc, item) => {
    /* !!! this line shows that if exception occurs, nothing happen -- the recover in for loop helps
     * to resolve and catch the exception */
    //val sumed = item
    
    val sumed = item.renderResult
    println(sumed.toString)
    acc + 1
  })


  /** Ideas **/
  /*
   * 1. basic flow
   *    for every number in list, multiply by 20 and then output to console via println
   */
  def basicFlow:RunnableGraph[Unit] = numberList.map(_ * 20).to(out) 

  /* 
   * 2. merge two set of list with same size, and contact it to another list, then transform the
   *    list to object, afterward, merge the list into string and then print it.
   */
  def mergeSourceFlow = numberList.zip(number2List).via(tupleSummer).to(out)
  
  //http://doc.akka.io/api/akka-stream-and-http-experimental/2.0.3/?_ga=1.202727313.1410241995.1456915045#akka.stream.scaladsl.Keep$
  //abstract def toMat[Mat2, Mat3](sink: Graph[SinkShape[Out], Mat2])(combine: (Mat, Mat2) ⇒ Mat3): ClosedMat[Mat3]
  /**
   * this shows to use the combine function, Keep is a quick util to help fetching different
   */
  def mergeSourceFlowWithMat = numberList.zip(number2List)
    .viaMat(tupleSummer)(Keep.right)
    .toMat(renderSum)((l, r) => r.map ( cnt => s"Total item processed: $cnt with renderResult executed = $l" ) )

  /* 3. run parallel - using graph
   *    a function takes 2 parameters, Int, String from two different source, and then
   *    group with result by batch with size 5, and then dispatch to different sink for processing
   *
   * 4. filtering
   *    for data other than something, discard and then printout
   *
   * 5. merge and execute two different branch
   */

  implicit val system = ActorSystem("AkkaFlowStudySystem")
  implicit val materialzer = ActorMaterializer()

  def main(args:Array[String]):Unit = {
    
    println("doing basicFlow")
    basicFlow.run()

    println("doing mergeSourceFlow")
    mergeSourceFlow.run()
    
    val matResult = mergeSourceFlowWithMat.run()
    (for {
      x <- matResult
    } yield {
      println(s"mergeSourceFlowWithMat result: $x")
      system.shutdown
    }) recover {
      case e:Exception => 
        println(s"got exception: ${e.getMessage}")
        system.shutdown
    }
  }
}


