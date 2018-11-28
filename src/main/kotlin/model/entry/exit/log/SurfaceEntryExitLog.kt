package model.entry.exit.log

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import model.*
import model.entryAndExitCode.MidwayEntryAndExitCodeController
import model.user.MidwayAuthAccountController
import org.sql2o.Sql2o

class SurfaceEntryExitLog(sql2o: Sql2o){
    private val auth = MidwayAuthAccountController(sql2o)
    private val logCon = MidwayEntryExitLog(sql2o)
    private val g = Gson()

    fun addLog(code: String, userToken: Token): Boolean{  //Getリクエストによって認証するのでJSONではない

        //ユーザーの認証
        val userId =auth.getIdForToken(userToken)
        return if(userId == null){
            //g.toJson(APIStatus(status = StatusEnumObj.ERROR, msg = "ユーザーIDが存在しません"))
            false
        }else{
            logCon.addLog(userId = userId.id, groupCode = code)
        }
    }


    //グループのすべてのユーザーのすべてのログを取得する
    fun getGroupAllUserAllLog(json: String):String{
        return try{
            val obj = g.fromJson(json, UserTokenAndGroupId::class.java)
            val list = logCon.getGroupAllUsersAllLog(token = Token(obj.userToken), groupId = obj.groupId)
            list?:g.toJson(APIStatus(StatusEnumObj.ERROR, msg = "エラー"))
            g.toJson(list)
        }catch(e: JsonSyntaxException){
            g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "JSON-ERROR"))
        }
    }

    //グループのユーザーのすべてのログを取得する
    fun getGroupUserAllLog(json: String){
        TODO("実装予定")
    }

    fun getUserAllLog(json: String):String{
        return try{
            val obj = g.fromJson(json, Token::class.java)
            val list = logCon.getUserAllOrLimitLog(Token(obj.token))
            if(list != null){
                g.toJson(list)
            }else{
                g.toJson(APIStatus(StatusEnumObj.ERROR, msg = "エラー"))
            }
        }catch(e: JsonSyntaxException){
            g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "JSONエラー"))
        }
    }

    fun getUserLimitLog(json: String):String{
        return try{
            val obj = g.fromJson(json, TokenAndLimit::class.java)
            val list = logCon.getUserAllOrLimitLog(Token(obj.token), limit = obj.limit)
            if(list != null){
                g.toJson(list)
            }else{
                g.toJson(APIStatus(StatusEnumObj.ERROR, msg ="エラーjsonの要素の型は大丈夫ですか？Tokenは有効ですか？"))
            }
        }catch(e: JsonSyntaxException){
            g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "jsonですよ？jsonの型は大丈夫ですか？お母さんは悲しいです・・"))
        }
    }

    //
}