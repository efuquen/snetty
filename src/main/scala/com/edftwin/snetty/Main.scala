package com.edftwin.snetty

import java.net.InetSocketAddress

import java.util.concurrent.Executors

import akka.actor._

import org.jboss.netty.channel.{
  ChannelPipelineFactory, ChannelPipeline, Channels}

import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

import org.jboss.netty.handler.codec.string.{
  StringDecoder, StringEncoder}
import org.jboss.netty.handler.codec.frame.{
  DelimiterBasedFrameDecoder, Delimiters}

import org.jboss.netty.util.CharsetUtil

import org.jboss.netty.bootstrap.ServerBootstrap

class HelloActor extends Actor {
  def receive = {
    case read: AkkaChannelRead[String] =>
      sender ! read.toWrite[String]("Hello " + read.data  + "!\n")
  }
}

object Main extends App {
  val system = ActorSystem("Snetty")
  val helloHandler = new AkkaActorChannelHandler[String,String](
    system,
    () => system.actorOf(Props[HelloActor])
  )

  val pipelineFactory = new ChannelPipelineFactory {
    override def getPipeline: ChannelPipeline = {
      val pipeline = Channels.pipeline

      // Decoders
      pipeline.addLast(
        "frameDecoder",
        new DelimiterBasedFrameDecoder(80, Delimiters.lineDelimiter: _*)
      )
      pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8))

      // Encoder
      pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8))
      pipeline.addLast("HelloHandler", helloHandler)

      pipeline
    }
  }

  val bootstrap = new ServerBootstrap(
    new NioServerSocketChannelFactory(
      Executors.newCachedThreadPool,
      Executors.newCachedThreadPool
  ))
  bootstrap.setPipelineFactory(pipelineFactory)
  bootstrap.bind(new InetSocketAddress("0.0.0.0", 8080))
}
