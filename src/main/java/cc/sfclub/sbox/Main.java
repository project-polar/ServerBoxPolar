package cc.sfclub.sbox;

import cc.sfclub.catcode.entities.At;
import cc.sfclub.catcode.entities.Image;
import cc.sfclub.catcode.entities.Plain;
import cc.sfclub.command.Node;
import cc.sfclub.command.Source;
import cc.sfclub.core.Core;
import cc.sfclub.events.Event;
import cc.sfclub.events.server.ServerStartedEvent;
import cc.sfclub.plugin.Plugin;
import cc.sfclub.plugin.SimpleConfig;
import cc.sfclub.service.ServiceProvider;
import cc.sfclub.user.User;
import cc.sfclub.user.UserManager;
import cc.sfclub.user.perm.Perm;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.greenrobot.eventbus.Subscribe;

import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

/*
     Plugin main class,it should extend Plugin.class.About specify Main class,see build.gradle
 */
public class Main extends Plugin {
    /*
        服务器完全加载完毕后触发
        在plugin.json中配置autoRegister后才有用。
     */
    private MountedServers servers;
    @Subscribe
    public void onServerStart(ServerStartedEvent event) {
        Vertx v = ServiceProvider.get(Vertx.class);
        try{
            Class.forName("cc.sfclub.pornbot.BotMain");
        }catch(Throwable t){
            ServiceProvider.get(Router.class).route().handler(BodyHandler.create());
        }
        ServiceProvider.get(Router.class).route("/api/mountedServers").handler(ctx -> ctx.response().end(servers.mounted.stream().collect(Collectors.joining("\n"))));
        registerCommand(Node.literal("sbox")
                .then(Node.literal("mount")
                        .requires(u -> u.getSender().hasPermission("serverbox.mount"))
                        .then(Node.argument("server_addr", Node.string()).executes(ctx -> {
                            String server = ctx.getArgument("server_addr",String.class);
                            if(!servers.mounted.contains(server)){
                                servers.mounted.add(server);
                                ctx.getSource().reply("Added.");
                            }else{
                                ctx.getSource().reply("Duplicated.");
                            }
                            return 0;
                        }))
                ).then(Node.literal("list")
                        .executes(ctx->{
                            ctx.getSource().reply("Currently mounted servers: \n"+servers.mounted.stream().collect(Collectors.joining("\n")));
                            return 0;
                        }))
                .then(Node.literal("remove")
                        .requires(u->u.getSender().hasPermission("serverbox.mount"))
                        .then(Node.argument("index",Node.integerArg())
                                .executes(ctx->{
                                    servers.mounted.remove(ctx.getArgument("index",int.class)-1);
                                    ctx.getSource().reply("Succeed.");
                                    return 0;
                                })))
        );
    }

    // 在此处存放插件被加载时的初始化逻辑，onEnable优先于onServerStart。
    @Override
    public void onEnable() {
        servers = ((SimpleConfig<MountedServers>)getConfig()).get();
    }

    // 请在这里处理插件卸载相关
    @Override
    public void onDisable() {
        getConfig().saveConfig();
    }
}
