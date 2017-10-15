package com.turingdi.awp;

import com.turingdi.awp.admin.LoginSubRouter;
import com.turingdi.awp.admin.OfficialAccountSubRouter;
import com.turingdi.awp.base.SubRouter;
import com.turingdi.awp.db.AccountService;
import com.turingdi.awp.util.common.Constants;
import com.turingdi.awp.verticle.WechatOauthSubRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-11 20:37.
 */
public class MainVerticle extends AbstractVerticle{
    @Override
    public void start() throws Exception {
        super.start();
        HttpServer server = vertx.createHttpServer();
        Router mainRouter = Router.router(vertx);
        mainRouter.route().handler(BodyHandler.create());
        //静态资源路由
        mainRouter.route("/static/*").handler(StaticHandler.create().setWebRoot("static"));
        mainRouter.route("/favicon.ico").handler(this::getLogo);
        AccountService accountSrv = new AccountService(vertx);
        Constants constants = new Constants();
        //微信授权的子路由
        SubRouter wechatOauthRouter = new WechatOauthSubRouter(accountSrv, constants).setVertx(vertx);
        mainRouter.mountSubRouter("/wxOauth", wechatOauthRouter.getSubRouter());
        //登录BMS的子路由
        SubRouter loginRouter = new LoginSubRouter(accountSrv, constants).setVertx(vertx);
        mainRouter.mountSubRouter("/bms/login", loginRouter.getSubRouter());
        JWTAuth jwtProvider = ((LoginSubRouter)loginRouter).getJWTProvider();
        //公众号配置子路由
        SubRouter offAccSubRouter = new OfficialAccountSubRouter(accountSrv, constants, jwtProvider).setVertx(vertx);
        mainRouter.mountSubRouter("/bms/offAcc", offAccSubRouter.getSubRouter());
        //TODO 其他子路由
        //如 mainRouter.mountSubRouter("/……"， ……);
        server.requestHandler(mainRouter::accept).listen(8083);
    }

    private void getLogo(RoutingContext rc) {
        rc.response().sendFile("static/img/favicon.ico").close();
    }
}
