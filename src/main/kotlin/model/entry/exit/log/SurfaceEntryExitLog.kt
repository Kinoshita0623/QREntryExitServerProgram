package model.entry.exit.log

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import model.APIStatus
import model.StatusEnumObj
import model.Token
import model.UserTokenAndGroupId
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

    fun getUserAllLog(json: String):String{
        return try{
            val obj = g.fromJson(json, UserTokenAndGroupId::class.java)
            val list = logCon.getUserAllLog(token = Token(obj.userToken), groupId = obj.groupId)
            g.toJson(list)
        }catch(e: JsonSyntaxException){
            g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "JSON-ERROR"))
        }
    }

    fun getAllUserAllLog(json: String){
        TODO("実装予定")
    }
}