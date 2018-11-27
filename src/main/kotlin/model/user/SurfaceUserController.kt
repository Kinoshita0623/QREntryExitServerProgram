package model.user

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import model.*
import org.sql2o.Sql2o

class SurfaceUserController(sql2o: Sql2o){
    private val authCon = MidwayAuthAccountController(sql2o)
    private val accountController = MidwayAccountController(sql2o)
    private val g = Gson()

    fun login(json: String): Token?{

        val obj = g.fromJson(json, LoginData::class.java)
        //obj?: return g.toJson(ResponseToken(token = null, status = StatusEnumObj.JSON_ERROR))

        return  authCon.login(id = obj.id, pw = obj.password)

    }

    fun logout(json: String): String{
        try{
            val obj = g.fromJson(json, Token::class.java)
            obj?: return g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "jsonの形式を確認してください"))

            return if(authCon.logout(obj)) {
                g.toJson(APIStatus(StatusEnumObj.SUCCESS, msg = "成功しました"))
            }else{
                g.toJson(APIStatus(StatusEnumObj.NOT_FOUND, msg ="対象のアカウントを発見できなかった"))
            }
        }catch(e: JsonSyntaxException){
            return g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "jsonの形式を確認してください"))
        }

    }

    fun createAccount(json: String):String{
        try{
            val obj = g.fromJson(json, AccountPost::class.java)
            obj?: return g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, "jsonエラー"))
            val b = accountController.makeAccount(id = obj.id, name = obj.name, password = obj.password)
            return if(b) g.toJson(APIStatus(StatusEnumObj.SUCCESS, msg ="成功しました")) else g.toJson(APIStatus(StatusEnumObj.ERROR,"エラー"))
        }catch(e: JsonSyntaxException){
            return g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, "jsonエラー"))
        }

    }

    fun getMyInfo(json: String):String{
        try{
            val token = g.fromJson(json,Token::class.java)
            token?: return g.toJson(APIStatus(StatusEnumObj.JSON_ERROR ,msg = "jsonの形式を確認してください"))
            val rawData = accountController.getPrivateAccountInfo(token)
            rawData?: return g.toJson(APIStatus(StatusEnumObj.ERROR , msg= "ログイン、アカウントは存在しますか？"))
            return g.toJson(rawData)
        }catch(e: JsonSyntaxException){
            return g.toJson(APIStatus(StatusEnumObj.JSON_ERROR ,msg = "jsonの形式を確認してください"))
        }


    }

    fun jsonTokenCheck(json: String):String{
        try{
            val token = g.fromJson(json, Token::class.java)
            token?: return g.toJson(ResponseToken(token = null, status = StatusEnumObj.JSON_ERROR))
            return tokenCheck(token)
        }catch(e: JsonSyntaxException){
            return g.toJson(ResponseToken(token = null, status = StatusEnumObj.JSON_ERROR))
        }
    }

    fun tokenCheck(token: Token):String{
        val account = authCon.checkLoginToken(token)
        return if(account != null){
            g.toJson(ResponseToken(token = account.token, status = StatusEnumObj.SUCCESS))
        }else{
            g.toJson(ResponseToken(token = null, status = StatusEnumObj.NOT_FOUND))
        }
    }

    fun searchUser(id: String):String?{
        //return g.toJson(accountController.searchUser(id))
        return accountController.searchUser(id)
    }
}