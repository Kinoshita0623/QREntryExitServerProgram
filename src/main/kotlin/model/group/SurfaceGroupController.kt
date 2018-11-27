package model.group

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import model.*
import org.sql2o.Sql2o

class SurfaceGroupController(sql2o: Sql2o){
    private val midwayGroup = MidwayGroupController(sql2o)
    //private val authUser = MidwayAuthAccountController(sql2o)

    private val g = Gson()

    fun createGroup(json: String): String{
        return try{
            val groupInfo = g.fromJson(json, CreateGroupInfo::class.java)
            when{
                groupInfo == null ->{
                    g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "送信したjsonを確認してください"))
                }!midwayGroup.createGroup(groupId = groupInfo.groupId, groupName = groupInfo.groupName, token = Token(groupInfo.userToken)) ->{
                g.toJson(APIStatus(StatusEnumObj.ERROR, msg = "作成に失敗しましたToken及びグループ名が重複していないか確認してください"))
            }else ->{ g.toJson(APIStatus(StatusEnumObj.SUCCESS, msg = "成功しました")) }
            }
        }catch(e: JsonSyntaxException){
            g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "送信したjsonを確認してください"))
        }

       // val token =authUser.getIdForToken(Token(groupInfo.userToken))
        //return

    }

    fun addMember(json: String): String{

        return try{
            val addInfo = g.fromJson(json, AddGroupInfo::class.java)
            when{
                addInfo == null ->{
                    g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "送信したjsonを確認してください"))
                }!midwayGroup.addMember(groupId = addInfo.groupId, groupToken = addInfo.groupToken, userId = addInfo.userId, authority = AutoAuthorityEnum().getAuthority(addInfo.authority)) ->{
                    g.toJson(APIStatus(StatusEnumObj.ERROR, msg = "送信データの内容を確認してください"))
                }else ->{
                    g.toJson(APIStatus(StatusEnumObj.SUCCESS, msg = "成功しました"))
                }
            }
        }catch(e: JsonSyntaxException){
            g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "送信したjsonを確認してください"))
        }
    }

    fun getGroupToken(json: String):String{
        //JsonSyntaxException
        try{
            val obj = g.fromJson(json, GetGroupToken::class.java)
            obj?: return g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "送信したjsonを確認してください"))

            val token = midwayGroup.getGroupToken(token = Token(obj.userToken), groupId = obj.groupId)?: return g.toJson(APIStatus(StatusEnumObj.ERROR, msg = "権限、アカウントのステータスを確認"))
            return g.toJson(Token(token = token))
        }catch(e: JsonSyntaxException){
            return g.toJson(APIStatus(StatusEnumObj.JSON_ERROR, msg = "送信したjsonを確認してください"))
        }


    }

}