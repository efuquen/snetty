package com.edftwin.snetty

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import org.jboss.netty.channel.{
  ChannelHandlerContext, MessageEvent, Channels,
  SimpleChannelHandler, DownstreamMessageEvent}

case class AkkaChannelWrite[V](
   data: V
)(
  implicit val ctx: ChannelHandlerContext,
  val e: MessageEvent
)

case class AkkaChannelRead[T](
   data: T
)(
  implicit val ctx: ChannelHandlerContext,
  val e: MessageEvent
) {
  def toWrite[V](writeData: V) = AkkaChannelWrite[V](writeData)(ctx,e)
}



class AkkaActorChannelHandler[T,V](
  system: ActorSystem,
  getActor: () => ActorRef
) extends SimpleChannelHandler {

  class AkkaChannelHandlerActor[T,V](
    readHandlingActor: ActorRef
  ) extends Actor {
    def receive = {
      case read:  AkkaChannelRead[T] =>
        readHandlingActor ! read
      case write: AkkaChannelWrite[V] =>
        val channel = write.e.getChannel
        val channelFuture = Channels.future(channel)
        val responseEvent = new DownstreamMessageEvent(
          channel, channelFuture, write.data, channel.getRemoteAddress
        )
        write.ctx.sendDownstream(responseEvent)
    }
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val readHandlingActor = getActor()
    val akkaChannelHandlerActor = system.actorOf(Props(new AkkaChannelHandlerActor[T,V](readHandlingActor)))
    akkaChannelHandlerActor ! AkkaChannelRead[T](e.getMessage.asInstanceOf[T])(ctx, e)
    super.messageReceived(ctx, e)
  }
}
