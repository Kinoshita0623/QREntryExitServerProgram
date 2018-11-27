package model.entryAndExitCode

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import model.*
import org.sql2o.Sql2o

class SurfaceEntryAndExitCodeController(sql2o: Sql2o){
    private val midwayEntryExit = MidwayEntryAndExitCodeController(sql2o = sql2o)
    private val g = Gson()

    fun changeOrCreateCode(json :String): String{
        try{
            val obj = g.fromJson(json, ManagementEntryExitCode::class.java)
            obj?:return g.toJson(EntryExitCode(groupId = null, entryExitCode = null, status = StatusEnumObj.JSON_ERROR))
            val entryExitCode = midwayEntryExit.changeXORCreateCode(obj.groupId, Token(obj.userToken))
            return if(entryExitCode == null){
                g.toJson(EntryExitCode(groupId = null, entryExitCode = null, status = StatusEnumObj.ERROR))
            }else{
                g.toJson(EntryExitCode(groupId = obj.groupId, entryExitCode = entryExitCode, status = StatusEnumObj.SUCCESS))
            }
        }catch(e: JsonSyntaxException){
            return g.toJson(EntryExitCode(groupId = null, entryExitCode = null, status = StatusEnumObj.JSON_ERROR))
        }

    }

    fun getCodePostType(json :String): String{
        return try{
            val info = g.fromJson(json, ManagementEntryExitCode::class.java)
            info?: g.toJson(EntryExitCode(groupId = null, entryExitCode = null, status = StatusEnumObj.JSON_ERROR))
            /*val code = midwayEntryExit.searchCodeFromGroupId(info.groupId, Token(info.userToken))
            code?: return g.toJson(EntryExitCode(groupId = null, entryExitCode = null, status = StatusEnumObj.ERROR))
            return  g.toJson(EntryExitCode(groupId = info.groupId, entryExitCode = code, status = StatusEnumObj.SUCCESS))*/
            getCodeGetType(info.groupId,userToken = Token(info.userToken))
        }catch(e: JsonSyntaxException){
            g.toJson(EntryExitCode(groupId = null, entryExitCode = null, status = StatusEnumObj.JSON_ERROR))
        }


    }

    fun getCodeGetType(groupId: String, userToken: Token):String{
        val code = midwayEntryExit.searchCodeFromGroupId(groupId, userToken)
        code?: return g.toJson(EntryExitCode(groupId = null, entryExitCode = null, status = StatusEnumObj.ERROR))
        return  g.toJson(EntryExitCode(groupId = groupId, entryExitCode = code, status = StatusEnumObj.SUCCESS))
    }

}