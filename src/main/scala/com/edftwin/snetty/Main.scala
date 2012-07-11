package com.edftwin.snetty

import akka.actor._

import org.jboss.netty.channel.{
  ChannelPipelineFactory, ChannelPipeline, Channels}

import org.jboss.netty.handler.codec.string.{
  StringDecoder, StringEncoder}
import org.jboss.netty.handler.codec.frame.{
  DelimiterBasedFrameDecoder, Delimiters}

import org.jboss.netty.util.CharsetUtil

class HelloActor extends Actor {
  def receive = {
    case read: AkkaChannelRead[String] =>
      sender ! read.toWrite[String]("Hello " + read.data  + "!")
  }
}

object Main extends App {
  val system = ActorSystem("Snetty")
  val helloHandler = new AkkaActorChannelHandler[String,String](
    () => system.actorOf(Props[HelloActor])
  )

  val piplineFactory = new ChannelPipelineFactory {
    override def getPipeline: ChannelPipeline = {
      val pipeline = Channels.pipeline

      // Decoders
      pipeline.addLast(
        "frameDecoder",
        new DelimiterBasedFrameDecoder(80, Array(Delimiters.lineDelimiter))
      )
      pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8))

      // Encoder
      pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8))
      pipeline.addLast("HelloHandler", helloHandler)

      pipeline
    }
  }
}
