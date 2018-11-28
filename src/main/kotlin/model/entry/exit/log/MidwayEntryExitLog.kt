package model.entry.exit.log

import model.EntryExitLogTimeData
import model.EntryExitLogTimeDataAndGroupId
import model.Token
import model.entryAndExitCode.MidwayEntryAndExitCodeController
import model.group.AuthorityEnum
import model.group.MidwayGroupController
import model.user.MidwayAuthAccountController
import org.sql2o.Sql2o

class MidwayEntryExitLog(sql2o: Sql2o){
    private val dbLog = DBEntryExitLog(sql2o)
    private val codeController = MidwayEntryAndExitCodeController(sql2o)
    private val midGroup = MidwayGroupController(sql2o)
    private val authUser = MidwayAuthAccountController(sql2o)

    fun addLog(userId: String, groupCode: String): Boolean{
        val groupId =codeController.searchGroupFromCode(groupCode)
        return if(groupId == null){
            false
        }else{
            dbLog.addLog(userId = userId, groupId = groupId)
        }
    }



    //ユーザーのグループ中のすべてのログを取得する
    fun getGroupUserLogAll(token: Token, groupId: String):List<EntryExitLogTimeData>?{
        val tokenAndId = authUser.getIdForToken(token)
        tokenAndId?: return null
        return dbLog.getUserInGroupLog(groupId = groupId, userId = tokenAndId.id)
    }

    //グループのすべてのユーザーのすべてのログを取得する
    fun getGroupAllUsersAllOrLimitLog(token: Token, groupId: String, limit:Int = -1):List<EntryExitLogTimeData>?{
        val tokenAndId = authUser.getIdForToken(token)
        tokenAndId?: return null
        val authority = midGroup.groupGetUserAuthority(userId = tokenAndId.id, groupId = groupId)
        if(authority == AuthorityEnum.ADMIN){
            return dbLog.getAllUsersInGroupAllOrLimitLog(groupId, limit)
        }
        return null
    }

    fun getGroupAllUsersAllLog(token: Token, groupId: String):List<EntryExitLogTimeData>?{
        return getGroupAllUsersAllOrLimitLog(token,groupId)
    }

    fun getUserAllOrLimitLog(token: Token, limit: Int = -1):List<EntryExitLogTimeDataAndGroupId>?{
        val tokenAndId = authUser.getIdForToken(token)
        tokenAndId?: return null
        return dbLog.getUserAllOrLimitLog(tokenAndId.id, limit)

    }
}