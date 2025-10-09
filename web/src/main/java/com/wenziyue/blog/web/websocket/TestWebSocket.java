//package com.wenziyue.blog.web.websocket;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.websocket.*;
//import javax.websocket.server.ServerEndpoint;
//
///**
// * @author wenziyue
// */
//@ServerEndpoint("/ws/chat")
//@Component
//@Slf4j
//public class TestWebSocket {
//    @OnOpen
//    public void onOpen(Session session) throws Exception {
//        log.info("WS opened, id={}", session.getId());
//        session.getBasicRemote().sendText("server: hello");
//    }
//
//    @OnMessage
//    public void onMessage(String msg, Session session) throws Exception {
//        log.info("WS recv from {} -> {}", session.getId(), msg);
//        session.getBasicRemote().sendText("server echo: " + msg);
//    }
//
//    @OnClose
//    public void onClose(Session session, CloseReason reason) {
//        // 可打印 reason
//    }
//
//    @OnError
//    public void onError(Session session, Throwable thr) {
//        thr.printStackTrace();
//    }
//}
