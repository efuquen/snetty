package com.edftwin.snetty

import akka.actor.{Actor, ActorRef}

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
  getActor: () => ActorRef
) extends SimpleChannelHandler with Actor {

  

  def receive = {
    case write: AkkaChannelWrite[V] =>
      val channel = write.e.getChannel
      val channelFuture = Channels.future(channel)
      val responseEvent = new DownstreamMessageEvent(
        channel, channelFuture, write.data, channel.getRemoteAddress
      )
      write.ctx.sendDownstream(responseEvent)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val actor = getActor()
    actor ! AkkaChannelRead[T](e.getMessage.asInstanceOf[T])(ctx, e)
    super.messageReceived(ctx, e)
  }
}
