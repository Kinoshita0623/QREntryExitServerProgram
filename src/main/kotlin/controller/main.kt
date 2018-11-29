package controller

//import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import model.ResponseToken
import model.StatusEnumObj
import model.Token
import model.entry.exit.log.SurfaceEntryExitLog
import model.entryAndExitCode.SurfaceEntryAndExitCodeController
import model.group.SurfaceGroupController
//import model.group.GroupController

import model.user.SurfaceUserController

import org.sql2o.Sql2o
import spark.Spark.*
import java.util.*


fun main(args: Array<String>){

    val sql2o = Sql2o("jdbc:h2:tcp://localhost/~/attendance","panta","")

    //val accountController = DBAccountController(sql2o)

    //val accountController = UserController(sql2o)
    val surfaceUserController = SurfaceUserController(sql2o)
    //val groupController = GroupController(sql2o)
    val surfaceGroupController = SurfaceGroupController(sql2o)
    val gson = Gson()
    val surfaceLog = SurfaceEntryExitLog(sql2o)
    val surfaceCode = SurfaceEntryAndExitCodeController(sql2o)

    port(Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("5353")))
    staticFileLocation("/public/example")





    options("/*"){ req, res->
        val accessControlRequestHeaders = req.headers("Access-Control-Request-Headers")
        if(accessControlRequestHeaders != null){
            res.header("Access-Control-Allow-Headers",accessControlRequestHeaders)
        }
        val accessControlRequestMethod = req.headers("Access-Control-Request-Method")
        if(accessControlRequestMethod != null){
            res.header("Access-Control-Allow-Methods",accessControlRequestMethod)
        }
        return@options "OK"
    }

    before("/*"){ _ ,res ->
        res.header("Access-Control-Allow-Origin","*")
        res.header("Access-Control-Request-Method","POST,GET,OPTIONS,DELETE,PUT")
        //res.header("Access-Control-Allow-Headers",)

       // res.type("application/json")
    }


    get("/create_account"){ _,res->
        res.redirect("/create_account.html")
    }
    get("/login"){_,res->
        res.redirect("/login.html")
    }

    get("/my_page"){_,res->
        res.redirect("/my_page.html")
    }
    get("/create_group"){_,res->
        res.redirect("/create_group.html")

    }

    get("/get_qr"){_,res->
        res.redirect("/get_qr.html")
    }


    //入退室のログを取る
    path("/access"){
        get("/:access"){ req, res ->
            val token = req.session().attribute<String>("token")
            if(token == null){
                res.redirect("/login")
            }else{
                surfaceLog.addLog(req.params(":access"),Token(token))
                res.redirect("/my_page.html")
            }

        }
    }


    //以下はjsonが返されます
    path("api"){


        path("/user"){

            path("/create"){
                get(""){ _,_->
                    "アカウント作成 json書式 {\"id\":\"アルファベットのID\",\"name\":\"名前\",\"password\":\"パスワード\"}"
                }

                post(""){req, _ ->
                    println("${req.body()} なんでおかしいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいい")
                    val re =surfaceUserController.createAccount(req.body())
                    println("戻り値いいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいいい$re")
                    re
                }
            }

            path("/login"){
                get(""){ _, _ ->
                    return@get "ログインページ jsonをpostしてください{\"id\":\"UserのID\",\"password\":\"UserのPassword\"}"
                }

                post(""){ req, _->

                    val token = surfaceUserController.login(req.body())
                    return@post if(token == null){
                        gson.toJson(ResponseToken(null,StatusEnumObj.ERROR))
                    }else{
                        req.session().attribute("token",token.token)
                        gson.toJson(ResponseToken(token.token,status = StatusEnumObj.SUCCESS))
                    }
                }

                delete(""){ req, _->
                    //return@delete accountController.deleteToken(req.body())
                    surfaceUserController.logout(req.body())

                }
            }

            get("/search/:user"){ req, _->

                surfaceUserController.searchUser(req.params("user"))

            }

            path("/i"){
                //アカウントのPrivateデータが転送されます
                post(""){ res, _ ->
                    surfaceUserController.getMyInfo(res.body())
                }
            }
        }


        path("/auth"){

            post(""){ res, _->
                //Tokenをpostしてチェックしたい場合
                surfaceUserController.jsonTokenCheck(res.body())

            }

            get(""){ req, _ ->

                val token: String? = req.session().attribute<String>("token")
                if(token != null){
                    return@get surfaceUserController.tokenCheck(Token(token))
                }
                gson.toJson(ResponseToken(token = null, status = StatusEnumObj.NOT_FOUND))

            }

        }

        path("/group"){
            //グループの操作

            path("/create"){
                get(""){ _, _ ->
                    "グループの作成"
                }

                post(""){req,_ ->
                    //return@post groupController.createGroup(req.body())
                    return@post surfaceGroupController.createGroup(req.body())
                }
            }

            path("/add"){
                get(""){ _,_->
                    "ユーザーをグループに加える"

                }
                post(""){ req, _ ->
                    //return@post groupController.addMember(req.body())
                    return@post surfaceGroupController.addMember(req.body())
                }
            }
            post("/token"){req,_->
                return@post surfaceGroupController.getGroupToken(req.body())


            }

        }


        path("/code"){
            get("/:groupId"){ req,_->
               return@get surfaceCode.getCodeGetType(req.params(":groupId"),Token(req.session().attribute("token")))
            }
            post(""){req,_->
                return@post surfaceCode.getCodePostType(req.body())
            }
            put(""){req,_->
                return@put surfaceCode.changeOrCreateCode(req.body())
            }
        }

        path("/log"){
            //入退出ログの取得操作機能をここに実装する

            //グループのすべてのユーザーのすべてのログを取得 要管理者
            post("/group-all"){ req, _->
                surfaceLog.getGroupAllUserAllLog(req.body())
            }

            //グループのすべてのユーザーを直近の指定回数分だけ取得する
            /*post("/group-count"){req,_res->

            }*/

            //グループの自分のすべてのログを取得する
            post("/mine-group-all"){req,_->
                surfaceLog.getGroupUserAllLog(req.body())
            }

            /*post("/mine-group-count"){req,_->

            }*/

            //グループを問わず
            post("/mine-all"){req,_->
                surfaceLog.getUserAllLog(req.body())
            }

            post("/mine-limit"){req,_->
                surfaceLog.getUserLimitLog(req.body())
            }






            /*post("/"){req,_->

            }*/



        }



    }
}

