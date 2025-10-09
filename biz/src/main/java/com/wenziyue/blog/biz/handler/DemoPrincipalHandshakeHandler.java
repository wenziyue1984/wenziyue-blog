//package com.wenziyue.blog.biz.handler;
//
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
//
//import java.security.Principal;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * @author wenziyue
// */
//public class DemoPrincipalHandshakeHandler extends DefaultHandshakeHandler {
//    @Override
//    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attrs) {
//        // 从查询参数 ?uid=1001 读取用户ID；没有就给个随机的，方便演示
//        String query = request.getURI().getQuery(); // 例如 "uid=1001"
//        String uid = null;
//        if (query != null) {
//            for (String p : query.split("&")) {
//                String[] kv = p.split("=", 2);
//                if (kv.length == 2 && "uid".equals(kv[0])) { uid = kv[1]; break; }
//            }
//        }
//        final String name = (uid != null && !uid.isEmpty()) ? uid : "guest-" + UUID.randomUUID();
//        return () -> name; // Principal::getName
//    }
//}
