package com.edftwin.snetty

import akka.actor.{Actor, ActorRef}

import org.jboss.netty.channel.{ChannelHandlerContext, MessageEvent}

class AkkaActorChannelHandler(
  getActor: (ChannelHandlerContext, MessageEvent) => ActorRef
) extends Actor {

  def receive = {
    case _ => None
  }
}
